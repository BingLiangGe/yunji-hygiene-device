package com.yunji.hygiene.entity.enums;

import lombok.Getter;

/**
 * @Project: yunji-cabinet-mall
 * @Package: com.yunji.cabinet.enums
 * @Title: OnlineStatus
 * @Description: 在线状态
 * <p>
 * History:
 * Date                     Version     Author          Summary
 * ============================================================
 * 2024-04-09 16:50:38      V1.0        XiaoZhang       新建类
 */

@Getter
public enum OnlineStatus {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线");
    private final Integer code;
    private final String text;

    OnlineStatus(Integer code, String text) {
        this.code = code;
        this.text = text;
    }
}
