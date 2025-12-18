package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * HeartBeatMsg：终端心跳消息（上行 0x0002）
 *
 * 特点：
 * - 心跳包通常没有消息体（body 为空），属于“空包”
 * - 该类主要用于类型分发：Decoder 识别 msgId 后构造 HeartBeatMsg，交给 HeartBeatMsgHandler 处理
 *
 * 业务常见处理：
 * - 刷新设备 lastSeen / 在线状态
 * - 必要时回平台通用应答（0x8001）或按协议要求回 ACK
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HeartBeatMsg extends DataPacket {

    /** 构造时交给父类 DataPacket 完成 header/payload 解析；心跳无 body，无需额外 parseBody */
    public HeartBeatMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }
}
