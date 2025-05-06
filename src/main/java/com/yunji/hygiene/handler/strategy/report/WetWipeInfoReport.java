package com.yunji.hygiene.handler.strategy.report;

import com.google.common.collect.Lists;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.domain.resp.report.WetWipeInfoReportResp;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.dto.WipeDeviceInfoDTO;
import com.yunji.hygiene.entity.po.ContainerCellPO;
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
public class WetWipeInfoReport extends AbsTranReportMsg {


    private static final long EVENT_DIFF_TIME = 10 * 60 * 1000;
    private static final int SLEEP_HOURS = 2000;

    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        log.info("WetWipeInfoReport channelRead0 msg:{}", JsonUtil.toJsonString(msg));
        String imei = msg.getHeader().getImei();
        WetWipeInfoReportResp sysInfo = (WetWipeInfoReportResp) msg;
        WipeDeviceInfoDTO devInfo = DeviceConvert.convert(sysInfo);
        if (devInfo.getEventId() != null && devInfo.getEventId() > 0) {
            Date updateTime = deviceService.getUpdateTime(devInfo.getEventId());
            updateTime = updateTime == null ? new Date() : updateTime;
            long diffTime = System.currentTimeMillis() - updateTime.getTime();
            if (diffTime < EVENT_DIFF_TIME) {
                log.debug("WetWipeInfoReport channelRead0 diffTime:{}", diffTime);
                // 拿到设备状态更新事件
                deviceService.updateEvent(devInfo.getEventId(), JsonUtil.toJsonString(devInfo));
                // 门全部关上 锁住 认为这个事件指令已经完成
                if (devInfo.getInLimitStatus() == 1 && devInfo.getLockStatus() == 1)
                    deviceService.eventFinish(devInfo.getEventId());
            }
        }
        if (devInfo.getSleepStatus() == 1) {
            SystemUtil.redisCache.set(DeviceCacheCode.DEVICE_SLEEP + imei, new Date(), SLEEP_HOURS, HOURS);
            log.info("WetWipeInfoReport sleep imei {}", imei);
        } else {
            SystemUtil.redisCache.delete(DeviceCacheCode.DEVICE_SLEEP + imei);
            log.info("WetWipeInfoReport wakeup imei {}", imei);
        }
        // 数据结果更新到缓存
        DeviceInfoCache.createInfo(devInfo);
        ContainerPO containerPO = deviceService.findByChipImei(imei);
        if (containerPO != null) {
            Integer battleStatus = containerPO.getBattleStatus();
            if (battleStatus == 1 && devInfo.getBattleLevel() <= 20) {
                containerPO.setBattleStatus(0);
                containerPO.setUpdateBattleTime(new Date());
                //  deviceService.updateBattle(containerPO.getId(), 0, new Date());
            } else if (battleStatus == 0 && devInfo.getBattleLevel() > 20) {
                containerPO.setBattleStatus(1);
                containerPO.setUpdateBattleTime(new Date());
                //  deviceService.updateBattle(containerPO.getId(), 1, new Date());
            }
            containerPO.setBattleLevel(devInfo.getBattleLevel());
            containerPO.setSleepStatus(devInfo.getSleepStatus());
            containerPO.setLockStatus(devInfo.getLockStatus());
            containerPO.setInLimitStatus(devInfo.getInLimitStatus());
            containerPO.setOutLimitStatus(devInfo.getOutLimitStatus());
            deviceService.updateCabinet(containerPO);
            // 更新状态到格子表
            ContainerCellPO cellPO = new ContainerCellPO();
            cellPO.setOrdinal(1);
            cellPO.setContainerId(containerPO.getId());
            cellPO.setDistance(devInfo.getDistance());
            deviceService.batchUpdateCell(Lists.newArrayList(cellPO));
            // 更新状态到售卖柜表
//            deviceService.updateCabinet(containerPO.getId(), devInfo.getBattleLevel(), devInfo.getSleepStatus()
//                    , devInfo.getRssi(), devInfo.getInLimitStatus(), devInfo.getOutLimitStatus(), devInfo.getLockStatus());
        }
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        log.info("WetWipeInfoReport resp:{} devInfo:{}", resp.getResult(), JsonUtil.toJsonString(devInfo));
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new WetWipeInfoReportResp(byteBuf);
    }
}
