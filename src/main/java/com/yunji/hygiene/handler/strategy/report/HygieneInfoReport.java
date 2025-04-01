package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.HygieneInfoReportResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.HygieneInfoDTO;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.handler.convert.DeviceConvert;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.service.DeviceInfoCache;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@Slf4j
public class HygieneInfoReport extends AbsTranReportMsg{

    private static final long EVENT_DIFF_TIME = 10 * 60 * 1000;
    private static final int SLEEP_HOURS = 2000;
    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        HygieneInfoReportResp sysInfo = (HygieneInfoReportResp) msg;
        log.info("HygieneInfoReport channelRead0 msg:{}", JsonUtil.toJsonString(msg));
        HygieneInfoDTO devInfo = DeviceConvert.convert(sysInfo,deviceService);
        if (devInfo.getEventId() != null && devInfo.getEventId() > 0) {
            Date updateTime = deviceService.getUpdateTime(devInfo.getEventId());
            updateTime = updateTime == null ? new Date() : updateTime;
            long diffTime = System.currentTimeMillis() - updateTime.getTime();
            if (diffTime < EVENT_DIFF_TIME) {
                log.debug("HygieneInfoReport channelRead0 diffTime:{}", diffTime);
                // 拿到设备状态更新事件
                deviceService.updateEvent(devInfo.getEventId(), JsonUtil.toJsonString(devInfo));
                // 门全部关上 锁住 认为这个事件指令已经完成
                if (devInfo.getLockStatus() == 1)
                    deviceService.eventFinish(devInfo.getEventId());
            }
        }
        // 数据结果更新到缓存
        DeviceInfoCache.createInfo(devInfo);
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        log.info("HygieneInfoReport channelRead0 msg:{}", msg);
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new HygieneInfoReportResp(byteBuf);
    }
}
