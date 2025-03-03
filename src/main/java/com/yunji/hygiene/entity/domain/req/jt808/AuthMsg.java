package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:鉴权包
 * @Version: 1.0
 */
@Data
public class AuthMsg extends DataPacket {

    private String authCode;//鉴权码

    public AuthMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }

    @Override
    public void parseBody() {
        this.setAuthCode(readString(this.payload.readableBytes()));
    }
}
