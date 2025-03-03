package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.LocationMsg;
import com.yunji.hygiene.entity.domain.resp.jt808.TerminalResp;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : peter-zhu
 * @date : 2025/2/28 16:03
 * @description : TODO
 **/
@Slf4j
@Component(HandleConstant.TERMINAL_RESP_SERVICE)
@ChannelHandler.Sharable
public class TerminalRespHandler extends AbsChannelReadHandler<TerminalResp> {

    @Override
    protected void readData(ChannelHandlerContext channelHandlerContext, DataPacket dataPacket) {
        TerminalResp resp = (TerminalResp) dataPacket;
        log.info("TerminalRespHandler readData:{}", JsonUtil.toJsonString(resp));
    }
}
