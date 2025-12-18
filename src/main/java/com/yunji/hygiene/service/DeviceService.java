package com.yunji.hygiene.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.constant.DeviceConstant;
import com.yunji.hygiene.constant.DeviceLockCode;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.enums.OnlineStatus;
import com.yunji.hygiene.entity.po.*;
import com.yunji.hygiene.repository.*;
import com.yunji.hygiene.util.JsonUtil;
import com.yunji.hygiene.util.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * DeviceService：设备域核心服务（设备在线状态、周期记录、柜子/格子更新、升级任务/文件缓存）
 *
 * 主要职责：
 * 1) 设备在线/离线：更新柜子状态 + 写入“在线周期”表（ContainerCycle）
 * 2) 设备/格子数据更新：柜子基础信息更新、格子批量更新
 * 3) 事件/任务：设备事件结束、更新 afterCmd、添加事件
 * 4) OTA 升级：升级文件缓存、创建升级缓存、开始升级、完成升级（释放锁+清理 redis 任务 key）
 *
 * 关键设计点：
 * - cabinetOffline 过滤 OLD_CHANNEL_SUFFIX：防止旧连接关闭导致把“新连接”误标离线
 * - handleCycle(force)：控制是否强制写周期（同状态重复上报通常不写，force 可强制写）
 * - upgradeFilecache：Guava 本地缓存，降低 DB 读取升级文件频率（max 50 / 120s）
 * - finishTask：升级完成后要更新两张表 + 删除 redis 任务 key + 解锁分布式锁
 */
@Slf4j
@Service
public class DeviceService {

    // -------------------- 仓储/DAO（设备域相关表） --------------------
    @Resource private IContainerRepository containerRep;
    @Resource private IContainerTypeRepository typeRepository;
    @Resource private IContainerCycleRepository cycleRep;
    @Resource private IContainerCellRepository cellRepo;
    @Resource private IDeviceEventRepository eventRepo;
    @Resource private IUpgradeFileRepository upgradeFileRepo;
    @Resource private IUpgradeTaskRepo upgradeTaskRepo;
    @Resource private IUpgradeInfoRepo upgradeInfoRepo;
    @Resource private IProductRepository productRepo;
    @Resource private INoticeImeiRepo noticeImeiRepo;

    // -------------------- 基础查询/辅助 --------------------

    /** 根据升级 infoId 查询关联的 fileId（用于升级链路） */
    public Long getFileByInfoId(Long infoId) {
        return upgradeFileRepo.getFileIdByInfoId(infoId);
    }

    /** 记录/初始化某类通知 imei（不存在则插入默认值） */
    public void noticeImei(String imei, int type) {
        NoticeImeiPO n = noticeImeiRepo.getNoticeImei(imei, type);
        if (n == null) noticeImeiRepo.save(new NoticeImeiPO(imei, type, 0));
    }

    /** 查询商品信息（设备上报/展示可能要用） */
    public ProductPO getProduct(Long productId) {
        return productRepo.getProduct(productId);
    }

    /** 按芯片 imei 查柜子（未删除） */
    public ContainerPO findByChipImei(String chip) {
        return containerRep.findByChipImeiAndDelFlag(chip, 0);
    }

    // -------------------- 设备在线/离线 --------------------

    /** 标记设备在线（默认非强制写周期） */
    public void cabinetOnline(String imei) {
        cabinetOnline(imei, false);
    }

    /**
     * 标记设备在线：
     * - 更新柜子 online 状态与时间
     * - 写入在线周期（cycle）
     */
    public void cabinetOnline(String imei, boolean force) {
        if (imei == null || imei.isEmpty()) return;

        int i = containerRep.cabinetOnline(imei, new Date());
        log.info("DeviceService cabinet online rs:{} imei:{}", i, imei);

        handleCycle(imei, OnlineStatus.ONLINE.getCode(), force);
    }

    /** 标记设备离线（默认非强制写周期） */
    public void cabinetOffline(String imei) {
        cabinetOffline(imei, false);
    }

    /**
     * 标记设备离线：
     * - 过滤旧连接后缀：避免旧连接关闭把新连接误标离线
     * - 更新柜子 offline 状态与时间
     * - 写入离线周期（cycle）
     */
    public void cabinetOffline(String imei, boolean force) {
        if (imei == null || imei.isEmpty() || imei.endsWith(ChannelManager.OLD_CHANNEL_SUFFIX)) return;

        int i = containerRep.cabinetOffline(imei, new Date());
        log.info("DeviceService cabinet offline rs:{} imei:{}", i, imei);

        handleCycle(imei, OnlineStatus.OFFLINE.getCode(), force);
    }

    /**
     * 写入“在线周期”记录：
     * - 查柜子是否存在
     * - 查最新周期 newestCycle
     * - 如果 newestCycle 为空：直接新增
     * - 如果状态变化（或 force==true）：先关闭旧周期（modifyNewestCycle），再新增新周期
     *
     * 注意：这里原本有分布式锁（DEVICE_CYCLE_LOCK）防并发重复写，当前被注释掉
     */
    public void handleCycle(String imei, Integer status, boolean force) {
        Date date = new Date();

        ContainerPO container = containerRep.findByChipImeiAndDelFlag(imei, 0);
        if (container != null) {
            ContainerCyclePO newestCycle = cycleRep.getNewestCycle(imei);
            log.debug("DeviceService cabinet newestCycle {}", JsonUtil.toJsonString(newestCycle));

            ContainerCyclePO cycle = new ContainerCyclePO();
            cycle.setContainerId(container.getId());
            cycle.setCycleType(status);
            cycle.setCreateTime(date);
            cycle.setChipImei(imei);
            cycle.setStartTime(date);

            if (newestCycle != null) {
                log.debug("DeviceService cabinet modifyNewestCycle {}", JsonUtil.toJsonString(newestCycle));
                if (force || !status.equals(newestCycle.getCycleType())) {
                    cycleRep.modifyNewestCycle(date, newestCycle.getId()); // 结束上一周期
                    cycleRep.save(cycle);                                  // 开启新周期
                }
            } else {
                log.debug("DeviceService cabinet save cycle {}", JsonUtil.toJsonString(cycle));
                cycleRep.save(cycle);
            }
        }
    }

    // -------------------- 柜子/格子数据更新（事务） --------------------

    /** 更新柜子信息（事务） */
    @Transactional
    public void updateCabinet(ContainerPO container) {
        containerRep.save(container);
    }

    /** 批量更新格子（事务） */
    @Transactional
    public void batchUpdateCell(List<ContainerCellPO> cells) {
        cellRepo.saveAll(cells);
    }

    /** 获取单个格子（事务） */
    @Transactional
    public ContainerCellPO getCell(Long containerId) {
        return cellRepo.getCell(containerId);
    }

    /** 获取格子列表（事务） */
    @Transactional
    public List<ContainerCellPO> getCellList(Long containerId) {
        return cellRepo.getCellList(containerId);
    }

    /** 更新单个格子（事务） */
    @Transactional
    public void updateCell(ContainerCellPO cell) {
        cellRepo.save(cell);
    }

    /** 更新设备版本号（上报版本后落库） */
    public void updateVersion(String imei, String version) {
        containerRep.updateVersion(imei, version);
    }

    // -------------------- 设备事件（任务） --------------------

    public Date getUpdateTime(Long eventId) {
        return eventRepo.getUpdateTime(eventId);
    }

    @Transactional
    public void eventFinish(Long eventId) {
        eventRepo.eventFinish(eventId, new Date());
    }

    @Transactional
    public void updateEvent(Long eventId, String afterCmd) {
        eventRepo.updateEvent(eventId, afterCmd, new Date());
    }

    @Transactional
    public void addEvent(String imei, String afterCmd) {
        eventRepo.addEvent(imei, afterCmd);
    }

    // -------------------- 升级文件缓存（Guava 本地缓存） --------------------

    /** OTA 文件本地缓存：减少 DB 压力（max 50 个，120s 未访问过期） */
    private static final Cache<Long, UpgradeFilePO> upgradeFilecache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(120, TimeUnit.SECONDS)
            .build();

    /** 根据 fileId 获取升级文件信息（优先本地缓存） */
    public UpgradeFilePO getFile(Long fileId) {
        UpgradeFilePO cached = upgradeFilecache.getIfPresent(fileId);
        if (cached == null) {
            UpgradeFilePO upgradeFile = upgradeFileRepo.getFileById(fileId);
            upgradeFilecache.put(fileId, upgradeFile);
            return upgradeFile;
        }
        return cached;
    }

    public UpgradeFilePO getFileByFileCodeLike(String fileCode) {
        return upgradeFileRepo.getFileByFileCodeLike(fileCode);
    }

    /** 解析版本前缀：厂商-项目-型号-芯片（用于匹配升级包） */
    public static String getVersionPrefix(String version) {
        String[] parts = version.split("-");
        if (parts.length >= 4) return String.join("-", parts[0], parts[1], parts[2], parts[3]);
        log.error("getVersionPrefix error version:{}", version);
        return version;
    }

    // -------------------- 升级任务状态与锁释放 --------------------

    /**
     * 完成升级任务（事务）：
     * - 通过 DeviceFileCache 获取当前 imei 的升级上下文（infoId/fileId/version...）
     * - 更新 upgradeInfo 与 upgradeTask 状态为完成
     * - 删除 Redis 中的升级任务 key（DEVICE_UPGRADE_TASK）
     * - 解锁 CABINET_UPGRADE_LOCK（避免后续升级被卡死）
     */
    @Transactional(rollbackFor = Exception.class)
    public void finishTask(String imei) {
        UpGradeFileDTO upGradeFileData = DeviceFileCache.getInfo(imei);
        if (upGradeFileData != null) {
            Long infoId = upGradeFileData.getInfoId();
            if (infoId != null && infoId > 0) {
                upgradeInfoRepo.finishTask(infoId);
                upgradeTaskRepo.finishTask(infoId);

                // Redis 任务 key：device:upgrade:task:{imei}:{infoId}
                String infoKey = imei + ":" + infoId;
                SystemUtil.redisCache().delete(DeviceCacheCode.DEVICE_UPGRADE_TASK + infoKey);

                // 解锁：LOCK_DATA:CABINET_UPGRADE_LOCK:{imei}
                LockUtil.unLockWithoutThread(DeviceLockCode.CABINET_UPGRADE_LOCK + imei);
            }
            // 可选：清理本地升级上下文缓存
            // DeviceFileCache.removeInfo(imei);
        }
    }

    // -------------------- 升级缓存构建（下发前准备） --------------------

    /** 通过 UpgradeCommandDTO 构建升级上下文缓存（返回是否成功） */
    public boolean createUpgradeCache(UpgradeCommandDTO cmd) {
        UpGradeFileDTO upgradeCache = createUpgradeCache(cmd.getImei(), cmd.getInfoId(), cmd.getFileId());
        return upgradeCache != null;
    }

    /**
     * 构建升级上下文缓存（DeviceFileCache）：
     * - 读取升级文件 UpgradeFilePO（DB 或本地缓存）
     * - 生成 versionPrefix：厂商-项目-型号-芯片（用于校验匹配）
     * - 缓存到 DeviceFileCache，供升级下发/分片发送/完成回调使用
     */
    public UpGradeFileDTO createUpgradeCache(String imei, Long infoId, Long fileId) {
        UpgradeFilePO file = getFile(fileId);
        if (file == null) return null;

        // 版本前缀：factoryBrand-PROJECT_CODE-modelType-chipType
        String versionPrefix = file.getFactoryBrand() + "-" + DeviceConstant.PROJECT_CODE + "-"
                + file.getModelType() + "-" + file.getChipType();

        UpGradeFileDTO upGradeFileData = new UpGradeFileDTO();
        upGradeFileData.setImei(imei);
        upGradeFileData.setFileId(fileId);
        upGradeFileData.setInfoId(infoId);
        upGradeFileData.setVersion(file.getVersion());
        upGradeFileData.setVersionPrefix(versionPrefix);

        DeviceFileCache.createInfo(upGradeFileData);
        log.info("UpgradeStrategy ready upgrade data:{}", upGradeFileData);
        return upGradeFileData;
    }

    // -------------------- 其他业务 --------------------

    public BigDecimal getTypeHeight(String typeCode) {
        return typeRepository.getTypeHeight(typeCode);
    }

    public void startUpgrade(Long infoId) {
        upgradeTaskRepo.startUpgrade(infoId);
        upgradeInfoRepo.startUpgrade(infoId);
    }
}
