package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.service.DeviceService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @author : peter-zhu
 * @date : 2025/2/15 09:15
 * @description : TODO
 **/
@Slf4j
public abstract class AbsTranReportMsg implements ITranReportMsg {

    @Resource
    protected DeviceService deviceService;

    public abstract TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg);

    public  ReportMsg getMsg(ByteBuf byteBuf) {
        return new ReportMsg(byteBuf);
    }
}
