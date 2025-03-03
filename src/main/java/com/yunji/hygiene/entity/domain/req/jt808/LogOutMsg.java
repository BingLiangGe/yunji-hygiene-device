package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:注销包 没有包体 空包
 * @Version: 1.0
 */
@Data
public class LogOutMsg extends DataPacket {
    public LogOutMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }
}
