package com.yunji.hygiene.constant;

/**
 * DeviceLockCode：分布式锁 Key 前缀常量
 *
 * 设计目的：
 * - 统一管理 Redis/Redisson 分布式锁 key，避免硬编码与冲突
 * - 通过固定前缀 + 场景后缀 + 业务维度（imei/containerId/taskId）组成最终锁 key
 *
 * 推荐拼接规范（示例）：
 * - LOCK_DATA:cycle:{imei}                 设备在线/离线状态更新互斥（防并发抖动）
 * - LOCK_DATA:CABINET_UPGRADE_LOCK:{imei}  柜机 OTA 升级互斥（同一设备同一时间只能升级一次）
 */
public class DeviceLockCode {

    /** 分布式锁 key 总前缀：用于区分普通缓存 key，便于统一清理/统计 */
    public static final String LOCK_CACHE_PREFIX = "LOCK_DATA:";

    // ======================== 设备锁 ========================

    /**
     * 设备在线离线锁前缀
     * - 典型用途：设备心跳/上报并发导致在线状态重复写、离线任务并发触发
     * - 推荐最终 key：LOCK_DATA:cycle:{imei}
     */
    public static final String DEVICE_CYCLE_LOCK = LOCK_CACHE_PREFIX + "cycle:";

    /**
     * 柜机升级锁前缀（OTA 升级互斥）
     * - 同一设备同一时间只允许一个升级任务进行
     * - 推荐最终 key：LOCK_DATA:CABINET_UPGRADE_LOCK:{imei}
     */
    public static final String CABINET_UPGRADE_LOCK = LOCK_CACHE_PREFIX + "CABINET_UPGRADE_LOCK:";
}
