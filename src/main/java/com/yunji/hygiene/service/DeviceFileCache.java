package com.yunji.hygiene.service;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import org.redisson.api.RMap;

/**
 * 设备文件缓存管理类，负责通过 Redis 缓存管理设备升级文件信息。
 * 使用 Redisson 提供的 RMap 实现设备升级文件信息的存取与移除。
 * 该缓存管理类是基于设备 IMEI 作为键（key）来存储设备升级文件信息的。
 */
public class DeviceFileCache {

    /**
     * 获取 Redisson 中保存设备升级文件信息的 RMap 对象
     * 使用 Redis 的哈希映射（RMap）来存储设备 IMEI 对应的升级文件信息。
     *
     * @return 返回 RMap 类型的设备升级文件缓存
     */
    private static RMap<String, UpGradeFileDTO> map() {
        // 获取 Redisson 客户端中对应的 Map，用于存储设备升级文件信息
        return SystemUtil.redisson.getMap(DeviceCacheCode.DEVICE_UPGRADE_FILE);
    }

    /**
     * 创建并存储设备的升级文件信息。
     * 将设备 IMEI 与对应的升级文件信息（UpGradeFileDTO）存入 Redis 中。
     *
     * @param info 设备的升级文件信息（包含 IMEI 和其他相关信息）
     */
    public static void createInfo(UpGradeFileDTO info) {
        // 将设备升级文件信息存入缓存
        map().put(info.getImei(), info);
    }

    /**
     * 移除设备的升级文件信息。
     * 根据设备 IMEI 从 Redis 中移除对应的升级文件信息。
     *
     * @param imei 设备的 IMEI 号，用于定位要移除的缓存信息
     */
    public static void removeInfo(String imei) {
        // 从缓存中移除指定设备 IMEI 的升级文件信息
        map().remove(imei);
    }

    /**
     * 获取设备的升级文件信息。
     * 根据设备 IMEI 获取存储在 Redis 中的设备升级文件信息（UpGradeFileDTO）。
     *
     * @param imei 设备的 IMEI 号
     * @return 返回设备的升级文件信息（UpGradeFileDTO），如果缓存中没有该设备的文件信息，则返回 null
     */
    public static UpGradeFileDTO getInfo(String imei) {
        // 从缓存中获取指定设备 IMEI 对应的升级文件信息
        return map().get(imei);
    }

}
