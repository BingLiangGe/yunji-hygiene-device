package com.yunji.hygiene.server;

import com.yunji.hygiene.codec.JT808Decoder;
import com.yunji.hygiene.codec.JT808Encoder;
import com.yunji.hygiene.constant.JT808Const;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.handler.strategy.jt808.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.yunji.hygiene.handler.strategy.jt808.*;


/**
 * JT808ChannelInitializer：Netty pipeline 初始化器（每个新连接都会执行一次）
 *
 * pipeline 结构（从上到下）：
 * 1) IdleStateHandler：读/写/总空闲检测（触发 IdleStateEvent，用于心跳、踢下线）
 * 2) DelimiterBasedFrameDecoder：按 0x7E 分隔符做“按帧拆包”（解决 TCP 粘包/拆包）
 * 3) JT808Decoder：反转义 + 校验 + 解析为 DataPacket 子类
 * 4) JT808Encoder：把 DataPacket 编码成 0x7E...0x7E 的 JT808 二进制
 * 5) ChannelReadHandler(businessGroup)：统一入站处理器，内部再按 msgId 分发到各业务 Handler
 *
 * 说明：
 * - businessGroup：把涉及 DB/HTTP/Redis 等阻塞操作的 handler 放到业务线程池，避免阻塞 workerGroup 的 IO 线程
 * - strategies：msgId -> AbsChannelReadHandler 映射，用于快速路由到具体处理器（Auth/Heart/Location/Register/Report...）
 */
@Component
public class JT808ChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    @Qualifier("businessGroup")
    private EventExecutorGroup businessGroup;

    @Resource private AuthMsgHandler authMsgHandler;
    @Resource private HeartBeatMsgHandler heartBeatMsgHandler;
    @Resource private LocationMsgHandler locationMsgHandler;
    @Resource private LogOutMsgHandler logOutMsgHandler;
    @Resource private RegisterMsgHandler registerMsgHandler;
    @Resource private ReportMsgHandler reportMsgHandler;
    @Resource private ChannelReadHandler readHandler;
    @Resource private TerminalRespHandler terminalRespHandler;

    /** msgId -> handler 策略映射（用于入站分发） */
    private static final Map<Short, AbsChannelReadHandler<? extends DataPacket>> strategies = new HashMap<>();

    /** 初始化策略映射：把 JT808 msgId 映射到对应的业务处理器 */
    public void init() {
        strategies.put(JT808Const.TERMINAL_RESP_COMMON, terminalRespHandler);
        strategies.put(JT808Const.TERMINAL_MSG_AUTH, authMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_HEARTBEAT, heartBeatMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_LOCATION, locationMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_LOGOUT, logOutMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_REGISTER, registerMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_REPORT, reportMsgHandler);
    }

    /** 根据 msgId 获取对应处理器（供 ChannelReadHandler 调用） */
    public static AbsChannelReadHandler<? extends DataPacket> getStrategy(short messageType) {
        return strategies.get(messageType);
    }

    @Value("${netty.time.all}")
    private long all;
    @Value("${netty.time.reader}")
    private long reader;
    @Value("${netty.time.writer}")
    private long writer;

    @Override
    protected void initChannel(SocketChannel ch) {
        // 注意：每个连接都会执行 initChannel；这里调用 init() 是为了确保策略映射可用
        init();

        ChannelPipeline pipeline = ch.pipeline();

        // 空闲检测：reader 秒未读/ writer 秒未写 / all 秒无读写，会触发 IdleStateEvent
        pipeline.addLast(new IdleStateHandler(reader, writer, all, TimeUnit.SECONDS));

        // 按 0x7E 分隔符拆帧：解决 TCP 粘包/拆包
        // maxFrameLength 1100：估算值（头12~16 + 体<=1023 + 分隔符2 + 转义冗余）
        pipeline.addLast(new DelimiterBasedFrameDecoder(
                1100,
                Unpooled.copiedBuffer(new byte[]{JT808Const.PKG_DELIMITER}),
                Unpooled.copiedBuffer(new byte[]{JT808Const.PKG_DELIMITER, JT808Const.PKG_DELIMITER})
        ));

        // 入站：ByteBuf -> DataPacket（还原转义、校验、解析 msgId 并构造子类）
        pipeline.addLast(new JT808Decoder());

        // 出站：DataPacket -> ByteBuf（写头/校验/转义/加 0x7E 包头包尾）
        pipeline.addLast(new JT808Encoder());

        // 统一入站业务处理：可能包含 DB/Redis 等操作，因此放入 businessGroup 防止阻塞 IO 线程
        pipeline.addLast(businessGroup, readHandler);
    }
}
