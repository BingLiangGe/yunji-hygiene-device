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
 * @author : peter-zhu
 * @date : 2025/2/12 15:03
 * @description : TODO
 **/
public class UpgradeStrategy implements ITransMsgStrategy {


    private static final Logger log = LoggerFactory.getLogger(UpgradeStrategy.class);

    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO writeData) {
        boolean b = LockUtil.lockWithoutThread(DeviceLockCode.CABINET_UPGRADE_LOCK + writeData.getImei(), 40, 120, TimeUnit.SECONDS);
        if (!b) {
            log.error("UpgradeStrategy strategyTranMsg the other data is leveling up :{}", JsonUtil.toJsonString(writeData));
            return null;
        }
        log.info("UpgradeStrategy strategyTranMsg getWriteData:{}", JsonUtil.toJsonString(writeData));
        UpGradeFileDTO info = DeviceFileCache.getInfo(writeData.getImei());
        log.info("UpgradeStrategy strategyTranMsg getInfo:{}", JsonUtil.toJsonString(info));
        OtaReadyTransMsg rs = new OtaReadyTransMsg();
        rs.setEventId(-1);
        rs.setMessageType(TransEnum.OTA_READY.getIssueType());
        rs.setVersionPrefix(info.getVersionPrefix());
        return rs;
    }
}
