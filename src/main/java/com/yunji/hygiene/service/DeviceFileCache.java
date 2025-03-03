package com.yunji.hygiene.service;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import org.redisson.api.RMap;

/**
 * @author : peter-zhu
 * @date : 2025/2/20 21:03
 * @description : TODO
 **/
public class DeviceFileCache {
    private static RMap<String, UpGradeFileDTO> map() {
        return SystemUtil.redisson.getMap(DeviceCacheCode.DEVICE_UPGRADE_FILE);
    }

    public static void createInfo(UpGradeFileDTO info) {
        map().put(info.getImei(), info);
    }

    public static void removeInfo(String imei) {
        map().remove(imei);
    }

    public static UpGradeFileDTO getInfo(String imei) {
        return map().get(imei);
    }

}
