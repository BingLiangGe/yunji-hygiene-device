package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.HeartBeatMsg;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 心跳消息->CommonResp
 * @Version: 1.0
 */

@Slf4j
@Component(HandleConstant.HEART_SERVICE)
@ChannelHandler.Sharable
public class HeartBeatMsgHandler extends AbsChannelReadHandler<HeartBeatMsg> {

    @Override
    protected void readData(ChannelHandlerContext ctx, DataPacket msg) {
        String imei = msg.getHeader().getImei();
        CommonResp resp = CommonResp.success(msg, getSerialNumber(ctx.channel()));
        log.debug("HeartBeatMsgHandler readData,imei:{} ", imei);
        deviceService.cabinetOnline(imei, false);
        write(ctx, resp);
    }
}
