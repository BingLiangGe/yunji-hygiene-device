package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.constant.DeviceLockCode;
import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.OtaReadyTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.enums.TransEnum;
import com.yunji.hygiene.service.DeviceFileCache;
import com.yunji.hygiene.util.JsonUtil;
import com.yunji.hygiene.util.LockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * UpgradeStrategy：升级指令策略（构造 OTA_READY 下发报文）
 *
 * 适用场景：
 * - 后台触发“设备升级”动作时，需要先下发 OTA_READY 指令给设备
 * - 设备收到后通常会进入升级准备阶段（例如校验版本前缀、切换升级模式等）
 *
 * 核心职责：
 * 1）对同一台设备加“升级互斥锁”，避免并发触发多次升级
 * 2）从 DeviceFileCache 获取该设备当前要升级的文件信息（版本前缀等）
 * 3）构造 OtaReadyTransMsg（透传消息），交给上层统一编码发送
 */
public class UpgradeStrategy implements ITransMsgStrategy {

    private static final Logger log = LoggerFactory.getLogger(UpgradeStrategy.class);

    /**
     * 构造“升级准备(OTA_READY)”透传消息
     *
     * @param writeData 下发指令入参（通常包含 imei、任务信息等）
     * @return TransMsg（具体为 OtaReadyTransMsg）；返回 null 表示当前设备正在升级中/获取不到锁
     */
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO writeData) {

        // 1）加升级互斥锁：同一 IMEI 在同一时间只允许一个升级流程
        // 参数含义（结合你们 LockUtil 的定义理解）：
        // - key：锁Key（按设备维度）
        // - waitTime：等待获取锁的时间（最多等40秒）
        // - leaseTime：锁的自动释放时间（120秒后自动释放，防止死锁）
        // - unit：时间单位
        boolean locked = LockUtil.lockWithoutThread(
                DeviceLockCode.CABINET_UPGRADE_LOCK + writeData.getImei(),
                40,
                120,
                TimeUnit.SECONDS
        );

        // 2）拿不到锁说明：可能已有升级流程正在进行（或别的线程正在处理同设备升级）
        // 这里直接返回 null，让上层决定如何提示/重试/排队
        if (!locked) {
            log.error("UpgradeStrategy strategyTranMsg the other data is leveling up :{}",
                    JsonUtil.toJsonString(writeData));
            return null;
        }

        // 3）打印下发参数，方便排查“谁触发了升级”
        log.info("UpgradeStrategy strategyTranMsg getWriteData:{}",
                JsonUtil.toJsonString(writeData));

        // 4）从缓存获取升级文件信息（通常在创建升级任务时写入）
        // 这里很关键：后续要用 versionPrefix 告诉设备“本次升级属于哪个版本族/机型/芯片类型”
        UpGradeFileDTO info = DeviceFileCache.getInfo(writeData.getImei());
        log.info("UpgradeStrategy strategyTranMsg getInfo:{}",
                JsonUtil.toJsonString(info));

        // ⚠️风险点提示：
        // 如果 info 为空，下面 info.getVersionPrefix() 会 NPE
        // 通常意味着：升级任务未正确创建缓存 / 缓存过期 / imei 不匹配
        // 你们如果希望更稳，可以在这里做非空校验并释放锁（看你们整体升级流程设计）

        // 5）构造 OTA_READY 透传消息
        // eventId = -1：代表不绑定业务事件（你们协议里 -1 通常表示“无事件/无需回写事件表”）
        // messageType：透传消息类型（OTA_READY）
        // versionPrefix：升级版本前缀（设备端用于校验/匹配升级包）
        OtaReadyTransMsg rs = new OtaReadyTransMsg();
        rs.setEventId(-1);
        rs.setMessageType(TransEnum.OTA_READY.getIssueType());
        rs.setVersionPrefix(info.getVersionPrefix());

        // 6）返回透传消息，由上层统一封装为 JT808 8900 下发报文并编码发送
        return rs;
    }
}
