package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.domain.resp.report.SysInfoReportResp;
import com.yunji.hygiene.entity.dto.DeviceInfoDTO;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.po.ContainerPO;
import com.yunji.hygiene.handler.convert.DeviceConvert;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.service.DeviceInfoCache;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * @author : peter-zhu
 * @date : 2025/2/15 11:49
 * @description : TODO
 **/
@Slf4j
@Service
public class SysInfoReport extends AbsTranReportMsg {


    private static final long EVENT_DIFF_TIME = 10 * 60 * 1000;
    private static final int SLEEP_HOURS = 2000;
    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        String imei = msg.getHeader().getImei();
        SysInfoReportResp sysInfo = (SysInfoReportResp) msg;
        log.info("ReportMsgHandler channelRead0 msg:{}", JsonUtil.toJsonString(msg));
        DeviceInfoDTO devInfo = DeviceConvert.convert(sysInfo);
        if (devInfo.getEventId() != null && devInfo.getEventId() > 0) {
            Date updateTime = deviceService.getUpdateTime(devInfo.getEventId());
            updateTime = updateTime == null ? new Date() : updateTime;
            long diffTime = System.currentTimeMillis() - updateTime.getTime();
            if (diffTime < EVENT_DIFF_TIME) {
                log.debug("ReportMsgHandler channelRead0 diffTime:{}", diffTime);
                // 拿到设备状态更新事件
                deviceService.updateEvent(devInfo.getEventId(), JsonUtil.toJsonString(devInfo));
                // 门全部关上 锁住 认为这个事件指令已经完成
                if (devInfo.getInLimitStatus() == 1 && devInfo.getLockStatus() == 1)
                    deviceService.eventFinish(devInfo.getEventId());
            }
        }
        if (devInfo.getSleepStatus() == 1) {
            log.info("handleReport sleep imei {}", imei);
            SystemUtil.redisCache.set(DeviceCacheCode.DEVICE_SLEEP + imei, new Date(), SLEEP_HOURS, HOURS);
        } else {
            SystemUtil.redisCache.delete(DeviceCacheCode.DEVICE_SLEEP + imei);
            log.info("handleReport wakeup imei {}", imei);
        }
        // 数据结果更新到缓存
        DeviceInfoCache.createInfo(devInfo);
        ContainerPO containerPO = deviceService.findByChipImei(imei);
        if (containerPO != null) {
            Integer battleStatus = containerPO.getBattleStatus();
            if (battleStatus == 1 && devInfo.getBattleLevel() <= 20) {
                deviceService.updateBattle(containerPO.getId(), 0, new Date());
            } else if (battleStatus == 0 && devInfo.getBattleLevel() > 20) {
                deviceService.updateBattle(containerPO.getId(), 1, new Date());
            }
            // 更新状态到格子表
            deviceService.updateCabinetCell(1, containerPO.getId(), devInfo.getDistance(),
                    devInfo.getInLimitStatus(), devInfo.getOutLimitStatus(), devInfo.getLockStatus());
            // 更新状态到售卖柜表
            deviceService.updateCabinet(containerPO.getId(), devInfo.getBattleLevel(), devInfo.getSleepStatus());
        }
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        log.info("ReportMsgHandler channelRead0 msg:{}", msg);
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new SysInfoReportResp(byteBuf);
    }
}
