package com.yunji.hygiene.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 设备信息传输对象，包含设备的基本信息、状态信息和其他相关数据。
 * @Version: 1.0
 */
@Data
public abstract class DeviceInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * IMEI 号：设备唯一标识符
     */
    private String imei;

    /**
     * 事件ID：每次设备发生的事件ID，用于标识某一事件
     */
    private Long eventId;

    /**
     * 睡眠状态：设备当前的睡眠状态（如0表示不睡眠，1表示睡眠等）
     */
    private Integer sleepStatus;

    /**
     * 锁状态：设备的锁状态（如0表示未锁定，1表示已锁定等）
     */
    private Integer lockStatus;

    /**
     * 电池等级：设备当前的电池电量等级（如0为最低，5为最高等）
     */
    private Integer battleLevel;

    /**
     * 信号强度：设备的信号强度值（如RSSI）
     */
    private Integer rssi;

    /**
     * 最后更新时间：记录设备最后的更新时间，通常用于设备状态的更新或同步
     */
    private Date lastTime;

    /**
     * 设备详细信息列表：包含设备的详细信息（如传感器状态、设备型号等）
     */
    private List<DeviceDetailInfoDTO> infoList;
}
