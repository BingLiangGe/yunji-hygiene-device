package com.yunji.hygiene.codec;

import com.yunji.hygiene.constant.JT808Const;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.*;
import com.yunji.hygiene.entity.domain.resp.jt808.TerminalResp;
import com.yunji.hygiene.handler.strategy.report.ITranReportMsg;
import com.yunji.hygiene.handler.strategy.report.TransReportFactory;
import com.yunji.hygiene.util.JT808Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * JT808 协议解码器：
 * 1) 上游一般需要先做“按 0x7E 分帧”（确保 in 里是一帧完整报文，不然这里会把粘包半包当一包处理）
 * 2) 本 Decoder 做：转义还原 -> 校验码 XOR 校验 -> 按 msgId 分发到不同 DataPacket -> parse()
 *
 * 转义规则：
 * 0x7d 0x01 -> 0x7d
 * 0x7d 0x02 -> 0x7e
 *
 * TODO 1、写出解析工具类、解析上报的类容以以及下发的内容、方便自已测试,写一个对应的controller模拟方法出来。
 *      2、最后时刻上报的内容、是什么样的、看能否拿到,从数据当中拿取。
 *      3、消息到底是怎么解析的、需要再进一步的析思考一下。
 *      4、从缓存里面拿数据、从数据库里面拿数据
 *      5、netty模拟类、模拟上万个设备进行连接以及上报（报指定类型的数据、然后后观查其状态。
 *      6、数据当中模拟上万字设备、然后进行连接判断。
 *      7、不同的上报事件类型（开补货门、关补货门、开出货门、关出货门、ping、重置）然后上报提令都是一样的吗？除了有货、无货、是一澡锁住之外都是一样的吗？上报类型是从哪里区分的？
 *
 *
 */
@Slf4j
public class JT808Decoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 注意：hexDump 可能非常耗性能，建议线上降为 debug 或做长度截断
        log.info("decode ip:{},hex:{}", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(in));
        try {
            DataPacket msg = decodeOne(in);
            if (msg != null) {
                out.add(msg);
            }
        } catch (Exception e) {
            log.error("JT808Decoder decode error: {}", e.getMessage(), e);
        }
    }

    /**
     * 解一帧 JT808 报文（假设上游已分帧）
     */
    private DataPacket decodeOne(ByteBuf in) {
        // JT808 头部最小长度保护（这里只是粗略保护，真正生产建议按消息体属性计算长度）
        if (in.readableBytes() < 12) {
            return null;
        }

        // 1) 取出原始字节（会消费 readerIndex）
        byte[] raw = new byte[in.readableBytes()];
        in.readBytes(raw);

        // 2) 转义还原（0x7d 0x01/0x02）
        ByteBuf unescaped = revert(raw);

        // 3) 校验：最后 1 字节为校验码（XOR），需要先排除后再计算
        byte pkgCheckSum = unescaped.getByte(unescaped.writerIndex() - 1);
        unescaped.writerIndex(unescaped.writerIndex() - 1); // 排除校验码
        byte calCheckSum = JT808Util.XorSumBytes(unescaped);

        if (pkgCheckSum != calCheckSum) {
            log.error("校验码错误,pkgCheckSum:{},calCheckSum:{}", pkgCheckSum, calCheckSum);
            ReferenceCountUtil.safeRelease(unescaped);
            return null;
        }

        // 4) 解析：按 msgId 分发到不同 DataPacket
        return parse(unescaped);
    }

    /**
     * 将接收到的“转义数据”还原（注意：若 raw 最后一个字节是 0x7d，需要防止 i+1 越界）
     */
    public ByteBuf revert(byte[] raw) {
        int len = raw.length;
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer(len); // 解析完成后应由下游/packet 负责释放

        for (int i = 0; i < len; i++) {
            byte b = raw[i];

            if (b == 0x7d) {
                // 防越界：异常报文末尾是 0x7d，会导致 raw[i+1] 越界
                if (i + 1 >= len) {
                    log.error("转义还原失败：末尾出现单独 0x7d，raw={}", ByteBufUtil.hexDump(raw));
                    buf.writeByte(b);
                    break;
                }

                byte next = raw[i + 1];
                if (next == 0x01) {          // 0x7d 0x01 -> 0x7d
                    buf.writeByte(0x7d);
                    i++;
                } else if (next == 0x02) {   // 0x7d 0x02 -> 0x7e
                    buf.writeByte(0x7e);
                    i++;
                } else {
                    // 非法转义：按原样写入（也可以选择直接丢包/close）
                    buf.writeByte(b);
                }
            } else {
                buf.writeByte(b);
            }
        }
        return buf;
    }

    /**
     * 按 JT808 msgId 分发到不同的消息对象并触发 parse()
     */
    public DataPacket parse(ByteBuf bb) {
        DataPacket packet;
        int i = bb.readerIndex();
        short msgId = bb.getShort(i); // getShort 不移动 readerIndex

        switch (msgId) {
            case JT808Const.TERMINAL_RESP_COMMON:
                packet = new TerminalResp(bb);
                break;
            case JT808Const.TERMINAL_MSG_HEARTBEAT:
                packet = new HeartBeatMsg(bb);
                break;
            case JT808Const.TERMINAL_MSG_LOCATION:
                packet = new LocationMsg(bb);
                break;
            case JT808Const.TERMINAL_MSG_REGISTER:
                packet = new RegisterMsg(bb);
                break;
            case JT808Const.TERMINAL_MSG_AUTH:
                packet = new AuthMsg(bb);
                break;
            case JT808Const.TERMINAL_MSG_LOGOUT:
                packet = new LogOutMsg(bb);
                break;
            case JT808Const.TERMINAL_MSG_REPORT:
                // 12 = msgId(2) + msgBodyProps(2) + phone(6) + flowId(2)
                byte messageType = bb.getByte(12);
                ITranReportMsg strategy = TransReportFactory.getStrategy(messageType);
                packet = strategy.getMsg(bb);
                break;
            default:
                packet = new DataPacket(bb);
                break;
        }

        // DataPacket 内部按协议字段读取 bb（通常会移动 readerIndex / 或基于 getXxx 读取）
        packet.parse();
        return packet;
    }

    public static void printByteBuff(ByteBuf msg) {
        ByteBuf duplicateByteBuf = msg.duplicate();
        int readableBytes = duplicateByteBuf.readableBytes();
        byte[] bytes = new byte[readableBytes];
        duplicateByteBuf.readBytes(bytes);

        for (int i = 0; i < readableBytes; i++) {
            byte byteValue = duplicateByteBuf.getByte(i);
            System.out.printf("Byte %d: 0x%02X\n", i, byteValue);
        }
        System.out.println(ByteBufUtil.hexDump(msg));
    }
}
