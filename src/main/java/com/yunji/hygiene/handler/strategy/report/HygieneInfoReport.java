package com.yunji.hygiene.handler.strategy.report;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.HygieneInfoReportResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.HygieneDetailInfoDTO;
import com.yunji.hygiene.entity.dto.HygieneInfoDTO;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.enums.DeviceErrorEnum;
import com.yunji.hygiene.entity.po.ContainerCellPO;
import com.yunji.hygiene.entity.po.ContainerPO;
import com.yunji.hygiene.handler.convert.DeviceConvert;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.service.DeviceInfoCache;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
        log.info("HygieneInfoReport channelRead0 msg:{}", JsonUtil.toJsonString(msg));
        HygieneInfoDTO devInfo = DeviceConvert.convert(sysInfo, deviceService);
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
        List<HygieneDetailInfoDTO> infoList = devInfo.getInfoList();
        List<ContainerCellPO> updateCellList = new ArrayList<>();
        int runtimeStatus = 1;
        List<String> errorMsg = Lists.newArrayList();
        for (HygieneDetailInfoDTO infoDTO : infoList) {
            Integer distance = infoDTO.getDistance();
            Integer motorStatus = infoDTO.getMotorStatus();
            boolean isInfraredNormal = !distance.equals(-1);
            boolean isMotorNormal = motorStatus.equals(1);
            String infraredMessage = isInfraredNormal ? STATUS_NORMAL : STATUS_ABNORMAL;
            String motorMessage = isMotorNormal ? STATUS_NORMAL : STATUS_ABNORMAL;
            runtimeStatus = (!isInfraredNormal || !isMotorNormal) ? 0 : 1;
            if (runtimeStatus == 0)
                errorMsg.add(MessageFormat.format(DeviceErrorEnum.CHECKED_202513.getMessage(), infoDTO.getOrdinal(), infraredMessage, motorMessage));
            ContainerCellPO cellPO = new ContainerCellPO();
            cellPO.setOrdinal(infoDTO.getOrdinal());
            cellPO.setDistance(distance);
            cellPO.setInfraredStatus(isInfraredNormal ? 0 : 1);
            updateCellList.add(cellPO);
        }
        // 数据结果更新到缓存
        DeviceInfoCache.createInfo(devInfo);
        ContainerPO containerPO = deviceService.findByChipImei(imei);
        if (containerPO != null) {
            containerPO.setRssi((int) sysInfo.getRssi());
            containerPO.setLockStatus((int) sysInfo.getLockStatus());
            containerPO.setRuntimeStatus(runtimeStatus);
            containerPO.setRuntimeError(Joiner.on(',').join(errorMsg));
            deviceService.updateCabinet(containerPO);
            for (ContainerCellPO cellPO : updateCellList) {
                deviceService.updateCell(cellPO.getOrdinal(), containerPO.getId(), cellPO.getDistance());
            }
        }
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        log.info("HygieneInfoReport resp:{} devInfo:{}", resp.getResult(), JsonUtil.toJsonString(devInfo));
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new HygieneInfoReportResp(byteBuf);
    }
}
