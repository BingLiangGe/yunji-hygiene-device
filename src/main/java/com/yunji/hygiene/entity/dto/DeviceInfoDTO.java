package com.yunji.hygiene.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public abstract class DeviceInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String imei;
    private Long eventId;
    private Integer sleepStatus;
    private Integer lockStatus;
    private Integer battleLevel;
    private Integer rssi; //信号强度
    private Date lastTime;
    private List<DeviceDetailInfoDTO> infoList;
}
