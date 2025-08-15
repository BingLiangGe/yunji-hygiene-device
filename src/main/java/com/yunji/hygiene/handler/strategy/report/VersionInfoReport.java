package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.domain.resp.report.GetVersionResp;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.enums.TransStrategyEnum;
import com.yunji.hygiene.entity.po.UpgradeFilePO;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.service.DeviceCallService;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author : peter-zhu
 * @date : 2025/2/19 16:14
 * @description : TODO
 **/
@Service
public class VersionInfoReport extends AbsTranReportMsg {
    private static final Logger log = LoggerFactory.getLogger(VersionInfoReport.class);

    @Resource
    private DeviceCallService deviceCallService;

    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        GetVersionResp vsp = (GetVersionResp) msg;
        String imei = msg.getHeader().getImei();
        log.info("VersionInfoReport vsp msg:{}", JsonUtil.toJsonString(vsp));
        deviceService.updateVersion(imei, vsp.getVersion());
        SystemUtil.redisCache().set(DeviceCacheCode.DEVICE_VERSION + imei, vsp.getVersion());
        CommonResp resp = CommonResp.success(msg, AbsChannelReadHandler.getSerialNumber(ctx.channel()));
        UpgradeFilePO newest = deviceService.getFileByFileCodeLike(DeviceService.getVersionPrefix(vsp.getVersion()));
        if (newest != null) {
            UpgradeCommandDTO upgradeCommandDTO = new UpgradeCommandDTO(TransStrategyEnum.DEVICE_GRADE.name(),
                    -1, imei, newest.getId(), -1L);
            deviceCallService.cacheFileUpgrade(upgradeCommandDTO);
        }
        return new TransReportDTO(true, true, resp);
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new GetVersionResp(byteBuf);
    }
}
