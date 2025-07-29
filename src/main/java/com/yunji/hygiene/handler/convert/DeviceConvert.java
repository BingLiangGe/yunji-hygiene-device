package com.yunji.hygiene.handler.convert;

import com.yunji.hygiene.constant.DeviceConstant;
import com.yunji.hygiene.entity.domain.resp.report.HygieneInfoReportResp;
import com.yunji.hygiene.entity.domain.resp.report.WetWipeInfoReportResp;
import com.yunji.hygiene.entity.dto.DeviceCellDetailDTO;
import com.yunji.hygiene.entity.dto.DeviceDetailInfoDTO;
import com.yunji.hygiene.entity.dto.HygieneInfoDTO;
import com.yunji.hygiene.entity.dto.WipeDeviceInfoDTO;

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
        devInfo.setUnexpectedOpen((msg.getInLimitStatus() >> 4) & 0x0F);
        devInfo.setInLimitStatus(msg.getInLimitStatus() & 0x0F);
        devInfo.setOutLimitStatus((int) msg.getOutLimitStatus());
        devInfo.setDistance(((msg.getDistanceMsb() & 0xFF) << 8) | (msg.getDistanceLsb() & 0xFF));
        devInfo.setBattleLevel((int) msg.getBattleLevel());
        devInfo.setTissueStatus((int) msg.getTissueStatus());
        devInfo.setRssi((int) msg.getRssi());
        devInfo.setLastTime(new Date());
        return devInfo;
    }

    public static HygieneInfoDTO convert(HygieneInfoReportResp msg, BigDecimal typeHeight) {
        HygieneInfoDTO devInfo = new HygieneInfoDTO();
        devInfo.setEventId((long) msg.getEventId());
        devInfo.setImei(msg.getHeader().getImei());
        devInfo.setSleepStatus(0);
        devInfo.setLockStatus((int) msg.getLockStatus());
        devInfo.setBattleLevel(0);
        devInfo.setRssi((int) msg.getRssi());
        List<DeviceDetailInfoDTO> list = new ArrayList<>();
        int ordinal = 0;
        for (Short distance : msg.getDistanceList()) {
            ordinal++;
            int tissueStatus = BigDecimal.valueOf(distance.intValue()).subtract(typeHeight).abs()
                    .compareTo(DeviceConstant.PRODUCT_DIFFER_VALUE) > 0 ? 1 : 0;
            list.add(new DeviceDetailInfoDTO(ordinal, distance.intValue(), 0, 1, (int) msg.getMotorStatusList().get(ordinal - 1),
                    null, null, null, null));
        }
        devInfo.setInfoList(list);
        devInfo.setLastTime(new Date());
        return devInfo;
    }

    public static void setCellMsg(DeviceDetailInfoDTO detailInfo, DeviceCellDetailDTO eventQuantity) {
        detailInfo.setDeviceQuantity(eventQuantity.getDeviceQuantity());
        detailInfo.setProductId(eventQuantity.getProductId());
        detailInfo.setProductName(eventQuantity.getProductName());
        detailInfo.setSku(eventQuantity.getSku());
    }

    public static void setCellMsg(WipeDeviceInfoDTO deviceInfo, DeviceCellDetailDTO eventQuantity) {
        deviceInfo.setDeviceQuantity(eventQuantity.getDeviceQuantity());
        deviceInfo.setProductId(eventQuantity.getProductId());
        deviceInfo.setProductName(eventQuantity.getProductName());
        deviceInfo.setSku(eventQuantity.getSku());
    }
}
