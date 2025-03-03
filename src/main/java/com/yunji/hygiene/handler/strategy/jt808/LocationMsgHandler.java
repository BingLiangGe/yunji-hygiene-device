package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.LocationMsg;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 位置消息->CommonResp
 * @Version: 1.0
 */

@Slf4j
@Component(HandleConstant.LOCATION_SERVICE)
@ChannelHandler.Sharable
public class LocationMsgHandler extends AbsChannelReadHandler<LocationMsg> {

    @Autowired
    @Qualifier("workerGroup")
    private NioEventLoopGroup workerGroup;

    @Override
    protected void readData(ChannelHandlerContext ctx, DataPacket msg)  {
        log.debug(msg.toString());
        CommonResp resp = CommonResp.success(msg, getSerialNumber(ctx.channel()));
        workerGroup.execute(() -> write(ctx, resp));//直接write是由businessGroup执行，换成workerGroup写可以少一些判断逻辑，略微提升性能
    }
}
