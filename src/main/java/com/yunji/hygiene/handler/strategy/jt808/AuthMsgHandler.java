package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.AuthMsg;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.enums.TransStrategyEnum;
import com.yunji.hygiene.service.DeviceCallService;
import com.yunji.hygiene.service.DeviceFileCache;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 鉴权消息->CommonResp
 * @Version: 1.0
 */

@Slf4j
@Component(HandleConstant.AUTH_SERVICE)
@ChannelHandler.Sharable
public class AuthMsgHandler extends AbsChannelReadHandler<AuthMsg> {
    @Resource
    private DeviceCallService deviceCallService;

    @Override
    protected void readData(ChannelHandlerContext ctx, DataPacket msg) {
        String imei = msg.getHeader().getImei();
        deviceService.cabinetOnline(imei);
        ChannelManager.add(imei, ctx.channel());
        CommonResp resp = CommonResp.success(msg, getSerialNumber(ctx.channel()));
        log.info("AuthMsgHandler readData,imei:{} ", imei);
        if (SystemUtil.redisCache().hasKey(DeviceCacheCode.DEVICE_UPGRADE_TASK + imei)) {
            UpGradeFileDTO info = DeviceFileCache.getInfo(imei);
            log.info("AuthMsgHandler hasKey DEVICE_UPGRADE_TASK start upgrade,imei:{} info:{}", imei, JsonUtil.toJsonString(info));
            if (info != null)
                deviceCallService.command(new UpgradeCommandDTO(TransStrategyEnum.DEVICE_GRADE.name(), -1, imei
                        , info.getFileId(), info.getInfoId()));
        }
        write(ctx, resp);
    }
}
