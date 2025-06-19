package com.yunji.hygiene.entity.domain;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.Data;

import java.util.Date;

/**
 * @author : peter-zhu
 * @date : 2025/1/16 19:46
 * @description : TODO
 **/
@Data
public class DeviceChannel {
    private String imei;
    private Channel channel;
    private String lastEvent;
    private Date lastTime;
    private String lastCmd;
    private String desc;

    public DeviceChannel(String imei, Channel channel) {
        this.imei = imei;
        this.channel = channel;
    }
}
