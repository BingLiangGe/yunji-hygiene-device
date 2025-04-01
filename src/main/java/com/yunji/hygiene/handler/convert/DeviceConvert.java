package com.yunji.hygiene.handler.convert;

import com.yunji.hygiene.entity.domain.resp.report.HygieneInfoReportResp;
import com.yunji.hygiene.entity.domain.resp.report.WetWipeInfoReportResp;
import com.yunji.hygiene.entity.dto.HygieneDetailInfoDTO;
import com.yunji.hygiene.entity.dto.HygieneInfoDTO;
import com.yunji.hygiene.entity.dto.WipeDeviceInfoDTO;
import com.yunji.hygiene.entity.enums.ContainerTypeEnum;
import com.yunji.hygiene.service.DeviceService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author : peter-zhu
 * @date : 2025/1/24 16:13
 * @description : TODO
 **/
public class DeviceConvert {
    public static WipeDeviceInfoDTO convert(WetWipeInfoReportResp msg) {
        WipeDeviceInfoDTO devInfo = new WipeDeviceInfoDTO();
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

    public static HygieneInfoDTO convert(HygieneInfoReportResp msg, DeviceService deviceService) {
        HygieneInfoDTO devInfo = new HygieneInfoDTO();
        devInfo.setEventId((long) msg.getEventId());
        devInfo.setImei(msg.getHeader().getImei());
        devInfo.setSleepStatus(0);
        devInfo.setLockStatus((int)msg.getLockStatus());
        devInfo.setBattleLevel(0);
        devInfo.setRssi((int) msg.getRssi());
        BigDecimal typeHeight = deviceService.getTypeHeight(ContainerTypeEnum.HYGIENE.getTypeCode());
        Integer ordinal = 0;
        List<HygieneDetailInfoDTO> list = new ArrayList<>();
        for (Short distance : msg.getDistanceList()) {
            ordinal ++;
            Integer tissueStatus = 0;
            // FIXME 偏差值
            if (BigDecimal.valueOf(distance.intValue()).subtract(typeHeight).abs().compareTo(BigDecimal.ONE) > 0)  {
                tissueStatus =1;
            }
            list.add(new HygieneDetailInfoDTO(ordinal, distance.intValue(), tissueStatus));
        }
        devInfo.setInfoList(list);
        devInfo.setLastTime(new Date());
        return devInfo;
    }
}
