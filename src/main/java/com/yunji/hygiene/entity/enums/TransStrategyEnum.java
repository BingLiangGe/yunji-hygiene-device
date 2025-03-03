package com.yunji.hygiene.entity.enums;

/**
 * @author : peter-zhu
 * @date : 2025/1/14 14:27
 * @description : TODO
 **/
public enum TransStrategyEnum {
    OPEN_RESTOCK,//打开补货仓门
    OPEN_SHIPPING,//打开出货仓门
    CLOSE_RESTOCK,// 关闭补货仓门
    CLOSE_SHIPPING,// 关闭出货仓门
    PING,//ping机器
    GET_DEVICE_INFO,// 获取设备信息
    GET_VERSION,//获取版本号
    DEVICE_GRADE//升级
    ;
}
