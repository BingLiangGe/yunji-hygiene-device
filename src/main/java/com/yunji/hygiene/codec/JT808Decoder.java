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
 * @Author: peter
 * @Date: 2025-01-16
 * @Description :JT808协议解码器,转义规则: 0x7d 0x01 -> 0x7d
 * 0x7d 0x02 -> 0x7e
 * @Version: 1.0
 */
@Slf4j
public class JT808Decoder extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        log.info("decode ip:{},hex:{}", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(in));
        try {
            DataPacket msg = decode(in);
            if (msg != null) {
                out.add(msg);
            }
        } catch (Exception e) {
            log.error("JT808Decoder decode :{}", e.getMessage(), e);
        }
    }

    private DataPacket decode(ByteBuf in) {
        if (in.readableBytes() < 12) { //包头最小长度
            return null;
        }
        //转义
        byte[] raw = new byte[in.readableBytes()];
        in.readBytes(raw);
        ByteBuf escape = revert(raw);
        //校验
        byte pkgCheckSum = escape.getByte(escape.writerIndex() - 1);
        escape.writerIndex(escape.writerIndex() - 1);//排除校验码
        byte calCheckSum = JT808Util.XorSumBytes(escape);
        if (pkgCheckSum != calCheckSum) {
            log.error("校验码错误,pkgCheckSum:{},calCheckSum:{}", pkgCheckSum, calCheckSum);
            ReferenceCountUtil.safeRelease(escape);
            return null;
        }
        //解码
        return parse(escape);
    }

    /**
     * 将接收到的原始转义数据还原
     */
    public ByteBuf revert(byte[] raw) {
        int len = raw.length;
        ByteBuf buf = ByteBufAllocator.DEFAULT.heapBuffer(len);//DataPacket parse方法回收
        for (int i = 0; i < len; i++) {
            //这里如果最后一位是0x7d会导致index溢出，说明原始报文转义有误
            if (raw[i] == 0x7d && raw[i + 1] == 0x01) {
                buf.writeByte(0x7d);
                i++;
            } else if (raw[i] == 0x7d && raw[i + 1] == 0x02) {
                buf.writeByte(0x7e);
                i++;
            } else {
                buf.writeByte(raw[i]);
            }
        }
        return buf;
    }

    public DataPacket parse(ByteBuf bb) {
        DataPacket packet;
        int i = bb.readerIndex();
        short msgId = bb.getShort(i);
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
//            case JT808Const.TERMINAL_MSG_TRANS:
//                byte messageType = bb.getByte(bb.readerIndex());
//                ITranAck strategy = TransAckFactory.getStrategy(messageType);
//                packet = strategy.getAck(messageType, bb);
//                break;
            case JT808Const.TERMINAL_MSG_REPORT:
                //printByteBuff(bb);
                byte messageType = bb.getByte(12);
                ITranReportMsg strategy = TransReportFactory.getStrategy(messageType);
                packet = strategy.getMsg(bb);
                break;
            default:
                packet = new DataPacket(bb);
                break;
        }
        packet.parse();
        return packet;
    }

    public static void printByteBuff(ByteBuf msg) {
        // 复制一个新的 ByteBuf 来读取数据，而不改变原 ByteBuf 的 readerIndex
        ByteBuf duplicateByteBuf = msg.duplicate();
        // 获取 ByteBuf 中的可读字节数
        int readableBytes = duplicateByteBuf.readableBytes();
        // 读取并打印所有可读字节
        byte[] bytes = new byte[readableBytes];
        duplicateByteBuf.readBytes(bytes);  // 将 ByteBuf 中的可读字节读取到 byte 数组中
        for (int i = 0; i < readableBytes; i++) {
            byte byteValue = duplicateByteBuf.getByte(i);  // 读取一个字节
            System.out.printf("Byte %d: 0x%02X\n", i, byteValue);  // 打印字节值和对应的十六进制
        }
        // 打印 byte 数组的内容
        System.out.println(ByteBufUtil.hexDump(msg));
    }
}
