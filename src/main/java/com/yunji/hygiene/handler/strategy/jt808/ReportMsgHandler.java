package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.handler.strategy.report.ITranReportMsg;
import com.yunji.hygiene.handler.strategy.report.TransReportFactory;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 注册消息->RegisterResp
 * @Version: 1.0
 */

@Slf4j
@Component(HandleConstant.REPORT_SERVICE)
@ChannelHandler.Sharable
public class ReportMsgHandler extends AbsChannelReadHandler<ReportMsg> {
    @Override
    protected void readData(ChannelHandlerContext ctx, DataPacket msg) {
        ReportMsg reportMsg = (ReportMsg) msg;
        log.info("ReportMsgHandler readData reportMsg:{}", JsonUtil.toJsonString(reportMsg));
        ITranReportMsg strategy = TransReportFactory.getStrategy(reportMsg.getMessageType());
        TransReportDTO transAckDTO = strategy.handleReport(ctx, reportMsg);
        if (transAckDTO != null && transAckDTO.isSuccess() && transAckDTO.isTrans()) {
            log.info("ReportMsgHandler readData transAckDTO:{}", transAckDTO);
            if (transAckDTO.getDataPacket() instanceof TransMsg) {
                TransMsg transMsg = (TransMsg) transAckDTO.getDataPacket();
                DataPacket packet = convertTransMsg(-1, ctx.channel(),
                        msg.getHeader().getImei(), transMsg);
                //JT808Decoder.printByteBuff(packet.getPayload());
                log.debug("ReportMsgHandler readData and write packet:{}", JsonUtil.toJsonString(packet));
                ctx.channel().writeAndFlush(packet);
            } else
                ctx.channel().writeAndFlush(transAckDTO.getDataPacket());
        }
    }
}
