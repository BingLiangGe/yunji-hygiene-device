package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:心跳包 没有包体 空包
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HeartBeatMsg extends DataPacket {
    public HeartBeatMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }
}
