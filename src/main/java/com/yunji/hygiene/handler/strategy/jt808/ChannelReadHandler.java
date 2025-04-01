package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.server.JT808ChannelInitializer;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.service.SystemUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
//    private ByteBuf buildHeader(String imei, byte msgId,S) {
//        ByteBuf message = Unpooled.buffer();
//        message.writeByte(msgId);  // 消息ID
//        message.writeByte((bodyLength + 8) >> 8);  // 高字节（消息体长度）
//        message.writeByte((bodyLength + 8) & 0xFF); // 低字节（消息体长度）
//        message.writeBytes(terminalPhone.getBytes());
//        return message;
//    }

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 为所有业务handler提供一些基础方法
 * @Version: 1.0
 */
@Service
@Slf4j
@ChannelHandler.Sharable
public class ChannelReadHandler extends SimpleChannelInboundHandler<DataPacket> {
    @Resource
    private DeviceService deviceService;

    //  protected abstract void readData(ChannelHandlerContext channelHandlerContext, T t);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DataPacket dataPacket) {
        try {
            AbsChannelReadHandler<? extends DataPacket> strategy = JT808ChannelInitializer.getStrategy(dataPacket.getHeader().getMsgId());
            strategy.readData(channelHandlerContext, dataPacket);
        } catch (Exception e) {
            String imei = ChannelManager.getImei(channelHandlerContext.channel());
            log.error("BaseChannelReadHandler imei:{}, error:{}", imei, e.getMessage(), e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("BaseChannelReadHandler channelActive tcp connected");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ctx.pipeline().last() == this) {
            Channel channel = ctx.channel();
            log.debug("BaseChannelReadHandler channelInactive：{}", ChannelManager.getImei(channel));
            super.channelInactive(ctx);
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //FIXME 可以通过state分别做处理,一般服务端在这里关闭连接节省资源，客户端发送心跳维持连接
            IdleState state = ((IdleStateEvent) evt).state();
            Channel channel = ctx.channel();
            String imei = ChannelManager.getImei(channel);
            if (state == IdleState.READER_IDLE) {
                if (!SystemUtil.redisCache.hasKey(DeviceCacheCode.DEVICE_SLEEP + imei)) {
                    log.error("客户端{}出现问题,非休眠且长时间没接到客户端数据,即将关闭连接 imei:{}", channel.remoteAddress(), imei);
                    ctx.close();
                    deviceService.cabinetOffline(imei);
                }
            } else if (state == IdleState.WRITER_IDLE) {
                log.error("客户端{}写入超时", channel.remoteAddress());
            } else if (state == IdleState.ALL_IDLE) {
                log.warn("客户端{}读取写入超时", channel.remoteAddress());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


}
