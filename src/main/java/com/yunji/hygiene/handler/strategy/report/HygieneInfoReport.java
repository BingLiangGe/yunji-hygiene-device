package com.yunji.hygiene.handler.strategy.report;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.HygieneInfoReportResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.DeviceCellDetailDTO;
import com.yunji.hygiene.entity.dto.DeviceDetailInfoDTO;
import com.yunji.hygiene.entity.dto.HygieneInfoDTO;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.enums.ContainerTypeEnum;
import com.yunji.hygiene.entity.enums.DeviceErrorEnum;
import com.yunji.hygiene.entity.po.ContainerCellPO;
import com.yunji.hygiene.entity.po.ContainerPO;
import com.yunji.hygiene.entity.po.ProductPO;
import com.yunji.hygiene.handler.calculate.CabinetCalculate;
import com.yunji.hygiene.handler.convert.DeviceConvert;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.service.DeviceInfoCache;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class HygieneInfoReport extends AbsTranReportMsg {

    private static final long EVENT_DIFF_TIME = 10 * 60 * 1000;
    private static final int SLEEP_HOURS = 2000;

    private static final String STATUS_NORMAL = "正常";
    private static final String STATUS_ABNORMAL = "异常";

    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        HygieneInfoReportResp sysInfo = (HygieneInfoReportResp) msg;
        String imei = msg.getHeader().getImei();
        boolean updateEvent = false, eventFinish = false;
        log.info("HygieneInfoReport channelRead0 msg:{}", JsonUtil.toJsonString(msg));
        BigDecimal typeHeight = deviceService.getTypeHeight(ContainerTypeEnum.HYGIENE.getTypeCode());
        HygieneInfoDTO devInfo = DeviceConvert.convert(sysInfo, typeHeight);
        if (devInfo.getEventId() != null && devInfo.getEventId() > 0) {
            Date updateTime = deviceService.getUpdateTime(devInfo.getEventId());
            updateTime = updateTime == null ? new Date() : updateTime;
            long diffTime = System.currentTimeMillis() - updateTime.getTime();
            if (diffTime < EVENT_DIFF_TIME) {
                log.debug("HygieneInfoReport channelRead0 diffTime:{}", diffTime);
                updateEvent = true;
                if (devInfo.getLockStatus() == 1)
                    eventFinish = true;
            }
        }
        List<DeviceDetailInfoDTO> infoList = devInfo.getInfoList();
        List<String> errorMsg = Lists.newArrayList();
        ContainerPO containerPO = deviceService.findByChipImei(imei);
        List<ContainerCellPO> cellList = deviceService.getCellList(containerPO.getId());
        Map<Integer, ContainerCellPO> cellMap =
                cellList.stream().collect(Collectors.toMap(ContainerCellPO::getOrdinal, a -> a));
        for (DeviceDetailInfoDTO detailInfo : infoList) {
            Integer distance = detailInfo.getDistance();
            Integer motorStatus = detailInfo.getMotorStatus();
            boolean isInfraredNormal = !distance.equals(-1);
            boolean isMotorNormal = motorStatus.equals(1);
            String infraredMessage = isInfraredNormal ? STATUS_NORMAL : STATUS_ABNORMAL;
            String motorMessage = isMotorNormal ? STATUS_NORMAL : STATUS_ABNORMAL;
            int runtimeStatus = (!isInfraredNormal || !isMotorNormal) ? 0 : 1;
            if (runtimeStatus == 0)
                errorMsg.add(MessageFormat.format(DeviceErrorEnum.CHECKED_202513.getMessage(), detailInfo.getOrdinal(), infraredMessage, motorMessage));
            ContainerCellPO cellPO = cellMap.get(detailInfo.getOrdinal());
            cellPO.setDistance(distance);
            cellPO.setInfraredStatus(isInfraredNormal ? 0 : 1);
            ProductPO product = deviceService.getProduct(cellPO.getProductId());
            DeviceCellDetailDTO eventQuantity = CabinetCalculate.getEventQuantity(cellPO, detailInfo.getDistance(), typeHeight, product);
            cellPO.setDeviceQuantity(eventQuantity.getDeviceQuantity());
            deviceService.updateCell(cellPO);
            detailInfo.setProductQuantity(cellPO.getProductQuantity());
            DeviceConvert.setCellMsg(detailInfo, eventQuantity);
        }
        containerPO.setRssi((int) sysInfo.getRssi());
        containerPO.setLockStatus((int) sysInfo.getLockStatus());
        containerPO.setRuntimeStatus(errorMsg.isEmpty() ? 1 : 0);
        containerPO.setRuntimeError(Joiner.on(',').join(errorMsg));
        deviceService.updateCabinet(containerPO);
        // 拿到设备状态更新事件
        if (updateEvent)
            deviceService.updateEvent(devInfo.getEventId(), JsonUtil.toJsonString(devInfo));
        // 门全部关上 锁住 认为这个事件指令已经完成
        if (eventFinish)
            deviceService.eventFinish(devInfo.getEventId());
        // 数据结果更新到缓存
        DeviceInfoCache.createInfo(devInfo);
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        log.info("HygieneInfoReport resp success:{} devInfo:{}", resp.getResult(), JsonUtil.toJsonString(devInfo));
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new HygieneInfoReportResp(byteBuf);
    }
}
