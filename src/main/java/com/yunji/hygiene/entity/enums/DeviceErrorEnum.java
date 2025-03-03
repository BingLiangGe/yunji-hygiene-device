package com.yunji.hygiene.entity.enums;

import lombok.Getter;

/**
 * @author : peter-zhu
 * @date : 2025/1/11 15:50
 * @description : TODO
 **/
@Getter
public enum DeviceErrorEnum {
    CHECKED_202501(202501, "硬件服务没有这个handle:{0},imei{1}"),
    CHECKED_202502(202502, "下发指令中没有这个指令:{0},imei{1}"),
    CHECKED_202503(202503, "设备channel不存在,imei:{0}"),
    CHECKED_202504(202504, "设备不在线,imei:{0}"),
    CHECKED_202505(202505, "imei不规范,imei:{0}"),
    CHECKED_202506(202506, "设备红外距离获取失败:{0}"),
    CHECKED_202507(202507, "设备状态获取失败:{0}"),
    CHECKED_202508(202508, "设备无货:{0}"),
    CHECKED_202509(202509, "通过cmd没有找到下发类型:{0}"),
    CHECKED_202510(202510, "该指令没有实现下发处理:{0}"),
    CHECKED_202511(202511, "该指令没有实现终端服务回应处理:{0}"),
    CHECKED_202512(202512, "硬件服务器异常:{0}"),



    CHECKED_212501(212501, "未找到设备的绑定信息"),
    CHECKED_212502(212502, "设备的imei不存在,imei{0}"),
    CHECKED_212503(212503, "设备的二维码信息不存在,imei{0}"),
    CHECKED_212504(212504, "设备信息不存在"),
    CHECKED_212505(212505, "设备的sn不存在,sn{0}"),
    CHECKED_212506(212506, "设备已经测试成功绑定,请解绑后再走工厂测试,imei{0}"),
    CHECKED_212507(212507, "绑定失败,绑定信息{0}"),
    CHECKED_212508(212508, "设备已经绑定,绑定信息{0}"),
    CHECKED_212509(212509, "未找到货柜类型信息,绑定信息{0}"),
    ;
    private final Integer code;
    private final String message;

    DeviceErrorEnum(Integer code, String message)
    {
        this.code = code;
        this.message = message;
    }
}
