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

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:
 * @Version: 1.0
 */
@Component
public class JT808ChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    @Qualifier("businessGroup")
    private EventExecutorGroup businessGroup;

    @Resource
    private AuthMsgHandler authMsgHandler;

    @Resource
    private HeartBeatMsgHandler heartBeatMsgHandler;

    @Resource
    private LocationMsgHandler locationMsgHandler;

    @Resource
    private LogOutMsgHandler logOutMsgHandler;

    @Resource
    private RegisterMsgHandler registerMsgHandler;

    @Resource
    private ReportMsgHandler reportMsgHandler;

    @Resource
    private ChannelReadHandler readHandler;

    @Resource
    private TerminalRespHandler terminalRespHandler;

    private static final Map<Short, AbsChannelReadHandler<? extends  DataPacket>> strategies = new HashMap<>();


    public void init() {
        strategies.put(JT808Const.TERMINAL_RESP_COMMON, terminalRespHandler);
        strategies.put(JT808Const.TERMINAL_MSG_AUTH, authMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_HEARTBEAT, heartBeatMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_LOCATION, locationMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_LOGOUT, logOutMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_REGISTER, registerMsgHandler);
        strategies.put(JT808Const.TERMINAL_MSG_REPORT, reportMsgHandler);
    }

    public static AbsChannelReadHandler<? extends DataPacket> getStrategy(short messageType) {
        Short msgType = messageType;
        return strategies.get(msgType);
    }

    @Value("${netty.time.all}")
    private long all;

    @Value("${netty.time.reader}")
    private long reader;

    @Value("${netty.time.writer}")
    private long writer;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        init();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new IdleStateHandler(reader, writer, all, TimeUnit.SECONDS));
        // jt808协议 包头最大长度16+ 包体最大长度1023+分隔符2+转义字符最大姑且算60 = 1100
        pipeline.addLast(
                new DelimiterBasedFrameDecoder(1100, Unpooled.copiedBuffer(new byte[]{JT808Const.PKG_DELIMITER}),
                        Unpooled.copiedBuffer(new byte[]{JT808Const.PKG_DELIMITER, JT808Const.PKG_DELIMITER})));
        pipeline.addLast(new JT808Decoder());
        pipeline.addLast(new JT808Encoder());
//        pipeline.addLast(heartBeatMsgHandler);
//        pipeline.addLast(businessGroup, locationMsgHandler);
//        pipeline.addLast(authMsgHandler);
//        pipeline.addLast(registerMsgHandler);
//        pipeline.addLast(logOutMsgHandler);
        //pipeline.addLast(businessGroup, issueMsgHandler);
        pipeline.addLast(businessGroup, readHandler);//因为ReportMsgHandler中涉及到数据库操作，所以放入businessGroup
        // 日志-本地调试用
        //pipeline.addLast(new OutboundLoggingHandler());
    }

}
