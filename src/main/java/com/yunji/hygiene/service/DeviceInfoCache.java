package com.yunji.hygiene.service;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.dto.DeviceInfoDTO;
import org.redisson.api.RMap;

/**
 * @author : peter-zhu
 * @date : 2025/1/15 16:33
 * @description : TODO
 **/
public class DeviceInfoCache {

    public static RMap<String, DeviceInfoDTO> map() {
        return SystemUtil.redisson.getMap(DeviceCacheCode.DEVICE_INFO_CACHE_CODE);
    }

    public static void createInfo(DeviceInfoDTO info) {
        map().put(info.getImei(), info);
    }

    public static void removeInfo(DeviceInfoDTO info) {
        map().remove(info.getImei());
    }

    public static DeviceInfoDTO getInfo(String imei) {
        return map().get(imei);
    }
}
