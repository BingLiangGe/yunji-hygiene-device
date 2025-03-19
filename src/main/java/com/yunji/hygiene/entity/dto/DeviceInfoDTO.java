package com.yunji.hygiene.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : peter-zhu
 * @date : 2025/1/24 15:24
 * @description : TODO
 **/
@Data
public class DeviceInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String imei;
    private Long eventId;
    private Integer sleepStatus;
    private Integer lockStatus;
    private Integer battleLevel;
    private Integer rssi; //信号强度
    private Date lastTime;
}
