package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author : peter-zhu
 * @date : 2025/2/13 13:53
 * @description : TODO
 **/
public interface ITranReportMsg {
    int PACKET_SIZE = 512;

    TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg);

    ReportMsg getMsg(ByteBuf byteBuf);
}
