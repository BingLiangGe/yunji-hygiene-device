package com.yunji.hygiene.constant;

/**
 * @author : peter-zhu
 * @date : 2025/2/17 17:02
 * @description : TODO
 **/
public class DeviceLockCode {
    public static final String LOCK_CACHE_PREFIX = "LOCK_DATA:";
    //------------------------设备锁-------------------------
    //在线离线锁
    public static final String DEVICE_CYCLE_LOCK = LOCK_CACHE_PREFIX + "cycle:";

    public static final String CABINET_UPGRADE_LOCK = LOCK_CACHE_PREFIX + "CABINET_UPGRADE_LOCK:";
}
