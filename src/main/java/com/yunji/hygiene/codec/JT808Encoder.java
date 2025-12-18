package com.yunji.hygiene.codec;

import com.yunji.hygiene.constant.JT808Const;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.util.JT808Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * JT808 协议编码器：
 * - 输入：业务对象 DataPacket
 * - 输出：可发送的 ByteBuf（已补齐校验码 + 转义 + 0x7E 边界）
 *
 * 转义规则：
 * - 0x7e -> 0x7d 0x02
 * - 0x7d -> 0x7d 0x01
 * TODO部份代码写死了、需要可变。
 */
@Slf4j
public class JT808Encoder extends MessageToByteEncoder<DataPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DataPacket msg, ByteBuf out) {
        try {
            // 1) 将 DataPacket 序列化为 ByteBuf（通常内部会预留 header 的部分字段占位）
            ByteBuf bb = msg.toByteBufMsg();

            // 2) 标记 writerIndex：后面需要回到头部覆盖写 msgId/bodyProps 等字段
            bb.markWriterIndex();

            // 3) bodyLen = 总长度 - 固定头长(12)
            short bodyLen = (short) (bb.readableBytes() - 12);

            // 4) 生成消息体属性（含 bodyLen/加密/分包/保留位等）
            short bodyProps = createDefaultMsgBodyProperty(bodyLen);

            // 5) 回到头部覆盖写（占用 4 字节：msgId + bodyProps）
            bb.writerIndex(0);
            bb.writeShort(msg.getHeader().getMsgId());
            int bodyPropsIndex = bb.writerIndex(); // 记录 bodyProps 的写入位置，后面可能 setShort 覆盖
            bb.writeShort(bodyProps);

            // 6) 回到原 writerIndex 继续写后续字段（保证不会把 body 覆盖掉）
            bb.resetWriterIndex();

            // 7) 特殊处理：透传类报文（TERMINAL_MSG_TRANS）需要把 payload 追加到正文中
            if (JT808Const.TERMINAL_MSG_TRANS == msg.getHeader().getMsgId() && msg.getPayload() != null) {
                short newBodyProps = (short) msg.getPayload().readableBytes(); // 透传时 bodyLen 以 payload 为准（按你们协议）
                bb.setShort(bodyPropsIndex, newBodyProps);
                bb.writeBytes(msg.getPayload());
                ReferenceCountUtil.safeRelease(msg.getPayload());
            }

            // 8) 写入 XOR 校验码（对校验码之前所有字节做异或）
            bb.writeByte(JT808Util.XorSumBytes(bb));

            // 9) 转义 + 加 0x7E 边界，得到最终发送帧
            log.info("encode1 :{},hex:{}\n", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(bb));
            ByteBuf escape = escape(bb);

            // 10) 写入出站 out（Netty 会发送 out）
            out.writeBytes(escape);

            // 11) 释放临时 buf（escape 内部已释放 raw=bb）
            ReferenceCountUtil.safeRelease(escape);
        } catch (Exception e) {
            log.error("JT808Encoder encode :{}", e.getMessage(), e);
        }
    }

    /**
     * 对待发送数据进行转义，并追加帧分隔符 0x7E（头尾各一个）。
     * 注意：本方法会读取 raw（推进 readerIndex），并在内部释放 raw。
     */
    public ByteBuf escape(ByteBuf raw) {
        int len = raw.readableBytes();
        ByteBuf buf = ByteBufAllocator.DEFAULT.directBuffer(len + 12);

        // 起始分隔符
        buf.writeByte(JT808Const.PKG_DELIMITER);

        // 逐字节转义
        while (len > 0) {
            byte b = raw.readByte();
            if (b == 0x7e) {
                buf.writeByte(0x7d);
                buf.writeByte(0x02);
            } else if (b == 0x7d) {
                buf.writeByte(0x7d);
                buf.writeByte(0x01);
            } else {
                buf.writeByte(b);
            }
            len--;
        }

        // raw 在这里释放（避免泄漏）
        ReferenceCountUtil.safeRelease(raw);

        // 结束分隔符
        buf.writeByte(JT808Const.PKG_DELIMITER);
        return buf;
    }

    /** 默认消息体属性：不加密/不分包/保留位=0 */
    public static short createDefaultMsgBodyProperty(short bodyLen) {
        return createMsgBodyProperty(bodyLen, (byte) 0, false, (byte) 0);
    }

    /**
     * 消息体属性（2字节）位运算：
     * - bodyLen: 10bit (0~1023)
     * - encType: 3bit  (bit10~12)
     * - subPkg:  1bit  (bit13)
     * - reversed:2bit  (bit14~15)
     */
    public static short createMsgBodyProperty(short bodyLen, byte encType, boolean isSubPackage, byte reversed) {
        int subPkg = isSubPackage ? 1 : 0;
        int ret = (bodyLen & 0x3FF)
                | ((encType << 10) & 0x1C00)
                | ((subPkg << 13) & 0x2000)
                | ((reversed << 14) & 0xC000);
        return (short) (ret & 0xffff);
    }
}
