package com.yunji.hygiene.entity.enums;

import lombok.Getter;

/**
 * @author : peter-zhu
 * @date : 2025/1/11 11:22
 * @description : TODO
 **/
@Getter
public enum TransEnum {
    HAND_SHARK("HAND_SHARK", (byte) 1, "????"),//保留不用
    RESTOCK("RESTOCK", (byte) 2, "补货仓门"),
    PING("PING", (byte) 3, "ping机器"),
    GET_VERSION("GET_VERSION", (byte) 4, "获取版本号"),
    SHOPPING("SHIPPING", (byte) 5, "出货仓门"),
    IR_DISTANCE("IR_DISTANCE", (byte) 6, "????"),//保留不用
    SYS_INFO("SYS_INFO", (byte) 7, "状态上报"),
    OTA_READY("OTA_READY", (byte) 10, "ota准备"),
    OTA_DATA_RECEIVE("OTA_DATA_RECEIVE", (byte) 11, "ota数据接受"),
    OTA_DATA_EOP("OTA_DATA_EOP", (byte) 12, "ota_eop");

    TransEnum(String cmd, byte issueType, String description) {
        this.cmd = cmd;
        this.issueType = issueType;
        this.description = description;
    }

    public static String getCmdByType(byte messageType) {
        for (TransEnum issueEnum : TransEnum.values()) {
            if (issueEnum.getIssueType() == messageType) {
                return issueEnum.getCmd();
            }
        }
        return null;
    }

    public static TransEnum getIssueEnumByCmd(String cmd) {
        for (TransEnum issueEnum : TransEnum.values()) {
            if (issueEnum.cmd.equals(cmd)) {
                return issueEnum;
            }
        }
        return null;
    }

    private final String cmd;
    // 下发类型
    private final byte issueType;
    private final String description;
}
