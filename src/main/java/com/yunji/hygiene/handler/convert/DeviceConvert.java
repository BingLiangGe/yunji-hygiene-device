package com.yunji.hygiene.handler.convert;

import com.yunji.hygiene.entity.domain.resp.report.SysInfoReportResp;
import com.yunji.hygiene.entity.dto.DeviceInfoDTO;

import java.util.Date;

/**
 * @author : peter-zhu
 * @date : 2025/1/24 16:13
 * @description : TODO
 **/
public class DeviceConvert {
    public static DeviceInfoDTO convert(SysInfoReportResp msg) {
        DeviceInfoDTO devInfo = new DeviceInfoDTO();
        devInfo.setEventId((long) msg.getEventId());
        devInfo.setImei(msg.getHeader().getImei());
        devInfo.setSleepStatus((int) msg.getSleepMode());
        devInfo.setLockStatus((int) msg.getLockStatus());
        devInfo.setInLimitStatus((int) msg.getInLimitStatus());
        devInfo.setOutLimitStatus((int) msg.getOutLimitStatus());
        devInfo.setDistance(((msg.getDistanceMsb() & 0xFF) << 8) | (msg.getDistanceLsb() & 0xFF));
        devInfo.setBattleLevel((int) msg.getBattleLevel());
        devInfo.setTissueStatus((int) msg.getTissueStatus());
        devInfo.setRssi((int) msg.getRssi());
        devInfo.setLastTime(new Date());
        return devInfo;
    }
}
