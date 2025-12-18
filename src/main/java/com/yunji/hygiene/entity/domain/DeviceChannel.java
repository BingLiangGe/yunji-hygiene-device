package com.yunji.hygiene.entity.domain;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.Date;

/**
 * @Author: peter-zhu
 * @Date: 2025/1/16 19:46
 * @Description: 设备通道类，用于表示设备与网络通道的关联，以及设备的相关信息。
 * @Version: 1.0
 */
@Data
public class DeviceChannel {

    /**
     * IMEI：设备的唯一标识符（国际移动设备身份码）
     */
    private String imei;

    /**
     * Netty Channel：用于设备与服务器之间通信的网络通道
     */
    private Channel channel;

    /**
     * 最后事件：记录与设备相关的最后一个事件（如设备操作类型）
     */
    private String lastEvent;

    /**
     * 最后更新时间：设备的最后一次活动时间
     */
    private Date lastTime;

    /**
     * 最后命令：设备接收到的最后一个命令
     */
    private String lastCmd;

    /**
     * 描述信息：设备的描述信息，通常用于记录设备的附加信息或备注
     */
    private String desc;

    /**
     * 构造方法，用于初始化设备与通道的关联
     *
     * @param imei   设备的IMEI号
     * @param channel 与设备关联的Netty Channel
     */
    public DeviceChannel(String imei, Channel channel) {
        this.imei = imei;
        this.channel = channel;
    }
}
