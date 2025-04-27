package com.yunji.hygiene.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.constant.DeviceConstant;
import com.yunji.hygiene.constant.DeviceLockCode;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.enums.OnlineStatus;
import com.yunji.hygiene.entity.po.ContainerCyclePO;
import com.yunji.hygiene.entity.po.ContainerPO;
import com.yunji.hygiene.entity.po.UpgradeFilePO;
import com.yunji.hygiene.repository.*;
import com.yunji.hygiene.util.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author : peter-zhu
 * @date : 2025/1/22 10:35
 * @description : TODO
 **/
@Slf4j
@Service
public class DeviceService {
    private final Logger logs = LoggerFactory.getLogger(this.getClass());

    @Resource
    private IContainerRepository containerRep;
    @Resource
    private IContainerTypeRepository typeRepository;
    @Resource
    private IContainerCycleRepository cycleRep;
    @Resource
    private IContainerCellRepository cellRepo;
    @Resource
    private IDeviceEventRepository eventRepo;
    @Resource
    private IUpgradeFileRepository upgradeFileRepo;
    @Resource
    private IUpgradeTaskRepo upgradeTaskRepo;
    @Resource
    private IUpgradeInfoRepo upgradeInfoRepo;

//    public Long findIdByChipImei(String chip) {
//        return containerRep.findIdByChipImeiAndDelFlag(chip, 0);
//    }

    public ContainerPO findByChipImei(String chip) {
        return containerRep.findByChipImeiAndDelFlag(chip, 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cabinetOnline(String imei) {
        containerRep.cabinetOnline(imei, new Date());
        handleCycle(imei, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cabinetOffline(String imei) {
        containerRep.cabinetOffline(imei, new Date());
        handleCycle(imei, false);
    }

    public void handleCycle(String imei, boolean online) {
        Date date = new Date();
        String key = DeviceLockCode.DEVICE_CYCLE_LOCK + imei;
        boolean getLock = LockUtil.tryLock(key, 3, 10, TimeUnit.SECONDS);
        try {
            if (getLock) {
                ContainerPO container = containerRep.findByChipImeiAndDelFlag(imei, 0);
                if (container != null) {
                    ContainerCyclePO cycle = new ContainerCyclePO();
                    cycle.setContainerId(container.getId());
                    cycle.setCreateTime(date);
                    cycle.setChipImei(imei);
                    cycle.setStartTime(date);
                    if (online) {
                        cycle.setCycleType(OnlineStatus.ONLINE.getCode());
                        Long newestCycleId = cycleRep.getNewestCycleId(imei, OnlineStatus.OFFLINE.getCode());
                        cycleRep.modifyNewestCycle(date, newestCycleId);
                    } else {
                        cycle.setCycleType(OnlineStatus.OFFLINE.getCode());
                        Long newestCycleId = cycleRep.getNewestCycleId(imei, OnlineStatus.ONLINE.getCode());
                        cycleRep.modifyNewestCycle(date, newestCycleId);
                    }
                    cycleRep.save(cycle);
                }
            }
        } finally {
            LockUtil.unlock(key);
        }
    }

    @Transactional
    public void updateCabinet(Long containerId, Integer battleLevel, Integer sleepStatus,Integer rssi,Integer inLimitStatus,Integer outLimitStatus,Integer lockStatus) {
        containerRep.updateCabinet(containerId, battleLevel, sleepStatus,rssi,inLimitStatus,outLimitStatus,lockStatus);
    }

    @Transactional
    public void updateCabinetCell(Integer ordinal, Long containerId, Integer distance) {
        cellRepo.cabinetCell(ordinal, containerId, distance);
    }

//    public String selectVersion(String chipImei) {
//        return containerRep.selectVersion(chipImei);
//    }

    public void updateVersion(String imei, String version) {
        containerRep.updateVersion(imei, version);
    }

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

    private static final Cache<Long, UpgradeFilePO> upgradeFilecache = CacheBuilder.newBuilder()
            .maximumSize(50) // 最大缓存数量
            .expireAfterAccess(120, TimeUnit.SECONDS) // 数据过期时间
            .build();

    public UpgradeFilePO getFile(Long fileId) {
        UpgradeFilePO otaFileFromCache = upgradeFilecache.getIfPresent(fileId);
        if (otaFileFromCache == null) {
            UpgradeFilePO upgradeFile = upgradeFileRepo.getFileById(fileId);
            upgradeFilecache.put(fileId, upgradeFile);
            return upgradeFile;
        }
        return otaFileFromCache;
    }


//    public void finishTask(Long infoId) {
//        upgradeTaskRepo.finishTask(infoId);
//        upgradeInfoRepo.finishTask(infoId);
//    }

    @Transactional(rollbackFor = Exception.class)
    public void finishTask(String imei) {
        UpGradeFileDTO upGradeFileData = DeviceFileCache.getInfo(imei);
        if (upGradeFileData != null) {
            Long infoId = upGradeFileData.getInfoId();
            upgradeInfoRepo.finishTask(infoId);
            upgradeTaskRepo.finishTask(infoId);
            SystemUtil.redisCache().delete(DeviceCacheCode.DEVICE_UPGRADE + imei);
            //删除升级任务
            SystemUtil.redisCache().delete(DeviceCacheCode.DEVICE_UPGRADE_TASK + imei);
            // 解锁任务key
            LockUtil.unLockWithoutThread(DeviceLockCode.CABINET_UPGRADE_LOCK + imei);
            // DeviceFileCache.removeInfo(imei);
        }
    }


    public void updateBattle(Long id, int battleStatus, Date updateBattleTime) {
        containerRep.updateBattle(id, battleStatus, updateBattleTime);
    }

    public boolean createUpgradeCache(UpgradeCommandDTO cmd) {
        UpgradeFilePO file = getFile(cmd.getFileId());
        if (file == null)
            return false;
        String versionPrefix = file.getFactoryBrand() + "-" + DeviceConstant.PROJECT_CODE + "-"
                + file.getModelType() + "-" + file.getChipType();
        UpGradeFileDTO upGradeFileData = new UpGradeFileDTO();
        upGradeFileData.setImei(cmd.getImei());
        upGradeFileData.setFileId(cmd.getFileId());
        upGradeFileData.setInfoId(cmd.getInfoId());
        upGradeFileData.setVersion(file.getVersion());
        upGradeFileData.setVersionPrefix(versionPrefix);
        DeviceFileCache.createInfo(upGradeFileData);
        log.info("UpgradeStrategy strategyTranMsg ready upgrade data:{}", upGradeFileData);
        return true;
    }

    public BigDecimal getTypeHeight(String typeCode) {
       return typeRepository.getTypeHeight(typeCode);
    }

    public void updateHygieneCabinet(Long id, Integer rssi, Integer lockStatus) {
        containerRep.updateHygieneCabinet(id,rssi,lockStatus);
    }

    public void updateCabinetRuntime(Long id, Integer runtimeStatus, String runtimeError) {
        containerRep.updateCabinetRuntime(id,runtimeStatus,runtimeError);
    }
}
