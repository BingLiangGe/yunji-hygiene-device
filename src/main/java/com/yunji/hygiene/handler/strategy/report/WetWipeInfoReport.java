package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.domain.resp.report.WetWipeInfoReportResp;
import com.yunji.hygiene.entity.dto.DeviceCellDetailDTO;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.dto.WipeDeviceInfoDTO;
import com.yunji.hygiene.entity.enums.ContainerTypeEnum;
import com.yunji.hygiene.entity.po.ContainerCellPO;
import com.yunji.hygiene.entity.po.ContainerPO;
import com.yunji.hygiene.entity.po.NoticeImeiPO;
import com.yunji.hygiene.entity.po.ProductPO;
import com.yunji.hygiene.handler.calculate.CabinetCalculate;
import com.yunji.hygiene.handler.convert.DeviceConvert;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.service.DeviceInfoCache;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private static final String ERROR_EXCEPTION = "商品数量异常";

    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        boolean updateEvent = false, eventFinish = false;
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
                updateEvent = true;
                // 门全部关上 锁住 认为这个事件指令已经完成
                if (devInfo.getInLimitStatus() == 1 && devInfo.getLockStatus() == 1)
                    eventFinish = true;
            }
        }
        if (devInfo.getSleepStatus() == 1) {
            SystemUtil.redisCache.set(DeviceCacheCode.DEVICE_SLEEP + imei, new Date(), SLEEP_HOURS, HOURS);
            log.info("WetWipeInfoReport sleep imei {}", imei);
        } else {
            SystemUtil.redisCache.delete(DeviceCacheCode.DEVICE_SLEEP + imei);
            log.info("WetWipeInfoReport wakeup imei {}", imei);
        }
        ContainerPO containerPO = deviceService.findByChipImei(imei);
        if (containerPO != null) {
            Integer battleStatus = containerPO.getBattleStatus();
            if (battleStatus == 1 && devInfo.getBattleLevel() <= 30) {
                containerPO.setBattleStatus(0);
                containerPO.setUpdateBattleTime(new Date());
                deviceService.saveNoticeImei(new NoticeImeiPO(containerPO.getChipImei(), 7));
            } else if (battleStatus == 0 && devInfo.getBattleLevel() > 30) {
                containerPO.setBattleStatus(1);
                containerPO.setUpdateBattleTime(new Date());
            }
            containerPO.setBattleLevel(devInfo.getBattleLevel());
            containerPO.setSleepStatus(devInfo.getSleepStatus());
            containerPO.setLockStatus(devInfo.getLockStatus());
            containerPO.setInLimitStatus(devInfo.getInLimitStatus());
            containerPO.setOutLimitStatus(devInfo.getOutLimitStatus());
            containerPO.setRssi(devInfo.getRssi());
            ContainerCellPO cellPO = deviceService.getCell(containerPO.getId());
            if (cellPO != null) {
                ProductPO product = deviceService.getProduct(cellPO.getProductId());
                BigDecimal typeHeight = deviceService.getTypeHeight(ContainerTypeEnum.WIPE.getTypeCode());
                DeviceCellDetailDTO eventQuantity = CabinetCalculate.getEventQuantity(cellPO, devInfo.getDistance(), typeHeight, product);
                eventQuantity.setProductName(product.getProductName());
                // 更新状态到格子表
                deviceService.updateCell(cellPO);
                devInfo.setProductQuantity(cellPO.getProductQuantity());
                DeviceConvert.setCellMsg(devInfo, eventQuantity);
                if ((devInfo.getProductQuantity() > 0 && devInfo.getTissueStatus() == 0) ||
                        (devInfo.getProductQuantity() == 0 && devInfo.getTissueStatus() == 1)) {
                    containerPO.setRuntimeStatus(0);
                    containerPO.setRuntimeError(ERROR_EXCEPTION);
                } else {
                    containerPO.setRuntimeStatus(1);
                    containerPO.setRuntimeError("");
                }
            } else {
                log.error("WetWipeInfoReport cellPO not exist id:{}", containerPO.getId());
            }
            deviceService.updateCabinet(containerPO);
        }
        // 拿到设备状态更新事件
        if (updateEvent)
            deviceService.updateEvent(devInfo.getEventId(), JsonUtil.toJsonString(devInfo));
        if (eventFinish)
            deviceService.eventFinish(devInfo.getEventId());
        if (devInfo.getUnexpectedOpen() == 1)
            deviceService.addEvent(devInfo.getImei(), JsonUtil.toJsonString(devInfo));
        // 数据结果更新到缓存
        DeviceInfoCache.createInfo(devInfo);
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        log.info("WetWipeInfoReport resp success:{} devInfo:{}", resp.getResult(), JsonUtil.toJsonString(devInfo));
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new WetWipeInfoReportResp(byteBuf);
    }
}
