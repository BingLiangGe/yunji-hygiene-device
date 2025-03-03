package com.yunji.hygiene.entity.enums;

import lombok.Getter;

/**
 * @author : peter-zhu
 * @date : 2025/2/15 11:45
 * @description : TODO
 **/
@Getter
public enum TransReportEnum {
    VERSION_INFO("VERSION_INFO", (byte) 4, "版本上报"),
    SYS_INFO("SYS_INFO", (byte) 7, "状态上报"),
    OTA_READY("OTA_READY", (byte) 10, "ota准备"),
    OTA_DATA_RECEIVE("OTA_DATA_RECEIVE", (byte) 11, "ota数据接受"),
    OTA_DATA_EOP("OTA_DATA_EOP", (byte) 12, "ota_eop");

    TransReportEnum(String cmd, byte issueType, String description) {
        this.cmd = cmd;
        this.issueType = issueType;
        this.description = description;
    }

    public static String getCmdByType(byte messageType) {
        for (TransReportEnum issueEnum : TransReportEnum.values()) {
            if (issueEnum.getIssueType() == messageType) {
                return issueEnum.getCmd();
            }
        }
        return null;
    }

    public static TransReportEnum getIssueEnumByCmd(String cmd) {
        for (TransReportEnum issueEnum : TransReportEnum.values()) {
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
