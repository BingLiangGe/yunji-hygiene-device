package com.yunji.hygiene.entity.enums;

import lombok.Getter;

/**
 * @author : peter-zhu
 * @date : 2025/1/10 10:38
 * @description : TODO
 **/
@Getter
public enum DeviceHandlerEnum {


    AUTH("AUTH", "AuthMsgHandler"),
    REGISTER("REGISTER", "RegisterMsgHandler"),
    HEART("HEART", "HeartBeatMsgHandler"),
    LOCATION("LOCATION", "LocationMsgHandler"),
    REPORT("REPORT", "RegisterMsgHandler"),
    LOG_OUT("LOG_OUT", "LogOutMsgHandler"),
    ISSUE("ISSUE", "IssueMsgHandler");

    DeviceHandlerEnum(String handle, String serviceName) {
        this.handle = handle;
        this.serviceName = serviceName;
    }

    private final String handle;
    private final String serviceName;
}
