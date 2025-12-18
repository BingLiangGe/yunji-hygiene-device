package com.yunji.hygiene.entity.domain.req.jt808;

import com.yunji.hygiene.entity.domain.DataPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * LogOutMsg：终端注销消息（上行 0x0003）
 *
 * 特点：
 * - 注销包通常没有消息体（body 为空），属于“空包”
 * - 该类主要用于类型分发：Decoder 识别 msgId 后构造 LogOutMsg，交给 LogOutMsgHandler 处理
 *
 * 业务常见处理：
 * - 更新设备在线状态为离线（lastSeen / online 标志）
 * - 清理 imei -> channel 映射（ChannelManager.removeByChannel/removeByImei）
 * - 视终端协议要求回平台通用应答（0x8001）后再关闭连接
 */
@Data
public class LogOutMsg extends DataPacket {

    /** 构造时交给父类 DataPacket 完成 header/payload 解析；注销无 body，无需额外 parseBody */
    public LogOutMsg(ByteBuf byteBuf) {
        super(byteBuf);
    }
}
