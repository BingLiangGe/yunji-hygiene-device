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
 * AbsChannelReadHandler：JT808 入站业务处理器抽象基类
 *
 * 核心能力：
 * 1) 提供每个 Channel 独立的流水号（flowId）生成：getSerialNumber()
 * 2) 封装统一的发送方法 write()：writeAndFlush + 失败回调日志
 * 3) 提供下发透传(0x8900)包的组装方法 convertTransMsg()：
 *    - 组 JT808 Header（msgId=0x8900, flowId, imei, msgBodyProps）
 *    - 组透传 payload（messageType + messageLength + packageLength + eventId + contentBytes）
 *
 * 说明：
 * - SERIAL_NUMBER 存在于 channel.attr 上：同一设备连接内递增，断线重连会从 0 重新开始
 * - readData(...) 由各子类实现：处理特定 msgId 的业务逻辑（注册/鉴权/心跳/上报等）
 */
@Slf4j
public abstract class AbsChannelReadHandler<T extends DataPacket> {

    @Resource
    protected DeviceService deviceService;

    /** 绑定到 Channel 的“流水号计数器”属性（每条连接独立维护） */
    public static final AttributeKey<AtomicInteger> SERIAL_NUMBER = AttributeKey.valueOf("serialNumber");

    /**
     * 获取并递增流水号（flowId）
     * - 优先从 Channel.attr 取 AtomicInteger
     * - 若不存在则初始化为 0
     * - 每次调用 +1，返回 short（用于 JT808 header.flowId）
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

    /**
     * 统一发送下行消息
     * - writeAndFlush 异步发送
     * - 失败时记录 cause，便于排查链路问题（离线/写缓冲区满/连接已关闭等）
     */
    public void write(ChannelHandlerContext ctx, DataPacket msg) {
        log.info("发送硬件tcp消息:{}", msg);
        ctx.writeAndFlush(msg).addListener(future -> {
            if (!future.isSuccess()) {
                log.error("发送硬件tcp失败", future.cause());
            }
        });
    }

    /**
     * 组装 0x8900 透传下发包（终端透传下行）
     *
     * @param eventId  事件ID（用于上下行闭环；-1 表示不携带 eventId）
     * @param channel  设备连接（用于生成 flowId）
     * @param imei     设备IMEI（写到 JT808 header 中）
     * @param transMsg 业务透传对象（负责提供 messageType 与 contentBytes）
     *
     * 透传 payload 格式（你们自定义）：
     *  - 1B  messageType
     *  - 4B  messageLength
     *  - 2B  packageLength
     *  - 4B  eventId（可选）
     *  - NB  contentBytes（可选）
     */
    public static DataPacket convertTransMsg(int eventId, Channel channel, String imei, TransMsg transMsg) {
        // 1) 构造 JT808 Header（0x8900）
        DataPacket.Header header = new DataPacket.Header();
        header.setMsgId(JT808Const.TERMINAL_MSG_TRANS);
        header.setFlowId(getSerialNumber(channel));
        header.setImei(imei);

        // msgBodyProps：这里直接写 (12 + packageLength)，等价于“把长度塞进低10位”
        // 注意：这是一种简化写法；更严谨应使用 createMsgBodyProperty(真实bodyLen,...)
        header.setMsgBodyProps((short) (12 + transMsg.getPackageLength()));
        transMsg.setHeader(header);

        // 2) 构造透传 payload
        ByteBuf payload = ByteBufAllocator.DEFAULT.heapBuffer();
        payload.writeByte(transMsg.getMessageType());
        payload.writeInt(transMsg.getMessageLength());
        payload.writeShort(transMsg.getPackageLength());

        if (eventId != -1) payload.writeInt(eventId);
        if (transMsg.getPackageLength() > 0) payload.writeBytes(transMsg.getContentBytes());

        // 3) 设置 payload，并返回 DataPacket（交给 Encoder 编码+转义+加 0x7E）
        transMsg.setPayload(payload);
        return transMsg;
    }

    /** 子类实现：处理入站消息（由统一 readHandler 分发到具体 Handler） */
    abstract void readData(ChannelHandlerContext channelHandlerContext, DataPacket dataPacket);
}
