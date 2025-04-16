package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author : peter-zhu
 * @date : 2025/2/13 13:53
 * @description : TODO
 **/
@Service
@Slf4j
public class OtaDataEopReport extends AbsTranReportMsg {
    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        String imei = msg.getHeader().getImei();
        log.info("【智能柜】 OtaDataEopReport 升级完成芯片码：{}", imei);
        deviceService.finishTask(imei);
//        OtaTransMsg otaTransMsg = new OtaTransMsg();
//        otaTransMsg.setMessageType(TransEnum.OTA_DATA_EOP.getIssueType());
//        otaTransMsg.setEventId(-1);
//        otaTransMsg.setChunk(new byte[0]);
        return new TransReportDTO(true, false, null);
    }
}
