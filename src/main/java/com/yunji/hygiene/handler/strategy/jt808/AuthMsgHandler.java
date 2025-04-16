package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.AuthMsg;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 鉴权消息->CommonResp
 * @Version: 1.0
 */

@Slf4j
@Component(HandleConstant.AUTH_SERVICE)
@ChannelHandler.Sharable
public class AuthMsgHandler extends AbsChannelReadHandler<AuthMsg> {
    @Override
    protected void readData(ChannelHandlerContext ctx, DataPacket msg) {
        ChannelManager.add(msg.getHeader().getImei(), ctx.channel());
        CommonResp resp = CommonResp.success(msg, getSerialNumber(ctx.channel()));
        log.info("AuthMsgHandler readData,imei:{} ", msg.getHeader().getImei());
        write(ctx, resp);
    }
}
