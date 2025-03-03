package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.constant.JT808Const;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.service.DeviceService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : peter-zhu
 * @date : 2025/2/20 15:42
 * @description : TODO
 **/
@Slf4j
public abstract class AbsChannelReadHandler<T extends DataPacket> {

    @Resource
    protected DeviceService deviceService;

    public static final AttributeKey<AtomicInteger> SERIAL_NUMBER = AttributeKey.valueOf("serialNumber");

    /**
     * 递增获取流水号
     */
    public static short getSerialNumber(Channel channel) {
        Attribute<AtomicInteger> flowIdAttr = channel.attr(SERIAL_NUMBER);
        AtomicInteger flowId = flowIdAttr.get();
        if (flowId == null) {
            flowId = new AtomicInteger(0);
            flowIdAttr.set(flowId);
        }
        return (short) flowId.incrementAndGet();
    }

    public void write(ChannelHandlerContext ctx, DataPacket msg) {
        log.debug("发送硬件tcp消息:{}", msg.toString());
        ctx.writeAndFlush(msg).addListener(future -> {
            if (!future.isSuccess()) {
                log.error("发送硬件tcp失败", future.cause());
            }
        });
    }

    public static DataPacket convertTransMsg(int eventId, Channel channel, String imei, TransMsg transMsg) {
        DataPacket.Header header = new DataPacket.Header();
        header.setMsgId(JT808Const.TERMINAL_MSG_TRANS);
        header.setFlowId(getSerialNumber(channel));
        header.setImei(imei);
        header.setMsgBodyProps((short) (12 + transMsg.getPackageLength()));
        transMsg.setHeader(header);
        ByteBuf payload = ByteBufAllocator.DEFAULT.heapBuffer();
        payload.writeByte(transMsg.getMessageType());
        payload.writeInt(transMsg.getMessageLength());
        payload.writeShort(transMsg.getPackageLength());
        if (eventId != -1)
            payload.writeInt(eventId);
        if (transMsg.getPackageLength() > 0)
            payload.writeBytes(transMsg.getContentBytes());
        transMsg.setPayload(payload);
        return transMsg;
    }

    abstract void readData(ChannelHandlerContext channelHandlerContext, DataPacket dataPacket);
}
