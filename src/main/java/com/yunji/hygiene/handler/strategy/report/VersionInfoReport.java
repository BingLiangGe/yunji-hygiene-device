package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.GetVersionResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author : peter-zhu
 * @date : 2025/2/19 16:14
 * @description : TODO
 **/
@Service
public class VersionInfoReport extends AbsTranReportMsg {
    private static final Logger log = LoggerFactory.getLogger(VersionInfoReport.class);
    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        GetVersionResp vsp = (GetVersionResp) msg;
        log.info("VersionInfoReport vsp msg:{}", JsonUtil.toJsonString(vsp));
        deviceService.updateVersion(msg.getHeader().getImei(),vsp.getVersion());
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new GetVersionResp(byteBuf);
    }
}
