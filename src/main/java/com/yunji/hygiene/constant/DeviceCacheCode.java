package com.yunji.hygiene.constant;

/**
 * DeviceCacheCode：设备相关 Redis Key 前缀定义
 *
 * 设计目的：
 * - 统一管理 Redis Key 命名，避免硬编码散落各处
 * - 通过分层前缀便于按模块清理、排查、统计（device:*）
 *
 * Key 结构约定（建议）：
 * - device:info:{imei}                设备基础信息缓存（在线信息/绑定信息/状态等）
 * - device:sleep:{imei}               设备休眠标记/休眠状态缓存
 * - device:upgrade:file:{fileId}      OTA 升级文件信息（OSS key/文件md5/大小等）
 * - device:upgrade:task:{imei}        OTA 升级任务信息（任务状态/进度/重试次数等）
 * - device:version:{imei}             设备固件/程序版本缓存
 */
public class DeviceCacheCode {

    /** 设备模块总前缀：device: */
    public static final String DEVICE_CACHE_CODE = "device:";

    /** 设备信息前缀：device:info: */
    public static final String DEVICE_INFO_CACHE_CODE = DEVICE_CACHE_CODE + "info:";

    /** 设备休眠状态前缀：device:sleep: */
    public static final String DEVICE_SLEEP = DEVICE_CACHE_CODE + "sleep:";

    /** OTA 升级文件信息前缀：device:upgrade:file: */
    public static final String DEVICE_UPGRADE_FILE = DEVICE_CACHE_CODE + "upgrade:file:";

    /** OTA 升级任务信息前缀：device:upgrade:task: */
    public static final String DEVICE_UPGRADE_TASK = DEVICE_CACHE_CODE + "upgrade:task:";

    /** 设备版本信息前缀：device:version: */
    public static final String DEVICE_VERSION = DEVICE_CACHE_CODE + "version:";
}
