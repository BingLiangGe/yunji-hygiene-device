package com.yunji.hygiene.service;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.dto.DeviceInfoDTO;
import org.redisson.api.RMap;

/**
 * @author : peter-zhu
 * @date : 2025/1/15 16:33
 * @description : 设备信息缓存类，用于操作设备信息缓存。基于 Redis Redisson 实现。
 *
 * 该类封装了对设备信息缓存（DeviceInfoCache）的操作，包括设备信息的存储、删除和获取操作。
 * 它使用了 Redisson 的 RMap 来存储设备信息，以便能够快速存取设备数据。
 */
public class DeviceInfoCache {

    /**
     * 获取 Redis 中的设备信息缓存 RMap。
     *
     * @return Redis 中存储设备信息的 RMap 数据结构，键为设备 IMEI，值为 DeviceInfoDTO。
     */
    public static RMap<String, DeviceInfoDTO> map() {
        // 获取 Redis 中的 Map 数据结构，使用设备信息缓存的键值对
        return SystemUtil.redisson.getMap(DeviceCacheCode.DEVICE_INFO_CACHE_CODE);
    }

    /**
     * 创建或更新设备信息，将设备信息存入缓存中。
     *
     * @param info 设备信息对象，包含设备的详细信息，键为设备 IMEI。
     */
    public static void createInfo(DeviceInfoDTO info) {
        // 将设备信息存储到 Redis 中，以 IMEI 作为键，DeviceInfoDTO 作为值
        map().put(info.getImei(), info);
    }

    /**
     * 从缓存中移除设备信息。
     *
     * @param info 需要删除的设备信息对象，依据 IMEI 删除对应的设备信息。
     */
    public static void removeInfo(DeviceInfoDTO info) {
        // 根据设备 IMEI 从缓存中删除设备信息
        map().remove(info.getImei());
    }

    /**
     * 根据设备 IMEI 获取设备信息。
     *
     * @param imei 设备的 IMEI，用于从缓存中查找对应的设备信息。
     * @return 返回存储在缓存中的设备信息，若未找到则返回 null。
     */
    public static DeviceInfoDTO getInfo(String imei) {
        // 根据 IMEI 从缓存中获取设备信息
        return map().get(imei);
    }
}
