package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * AuthMsg：终端鉴权消息（上行 0x0102）
 *
 * 作用：
 * - 终端在注册成功后，使用平台下发的“鉴权码”进行鉴权
 * - 平台收到后校验鉴权码，并将该连接绑定到设备身份（如 imei/channel 绑定）
 *
 * 解析说明：
 * - DataPacket 已解析完 header，并把消息体放到 payload（ByteBuf）
 * - AuthMsg.parseBody() 只负责解析 payload（消息体内容）
 * - 鉴权包 body 通常只有一个字段：鉴权码（字符串，长度=payload剩余全部字节）
 */
@Data
public class AuthMsg extends DataPacket {

    /** 鉴权码（终端携带的平台下发 authCode） */
    private String authCode;

    public AuthMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }

    /**
     * 解析消息体（payload）
     * - 直接把 payload 剩余所有字节按字符串读取，作为鉴权码
     * - readString 内部一般会按 JT808 默认字符集（GBK）解码
     */
    @Override
    public void parseBody() {
        this.setAuthCode(readString(this.payload.readableBytes()));
    }
}
