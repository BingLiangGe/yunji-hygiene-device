package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.RegisterMsg;
import com.yunji.hygiene.entity.domain.resp.jt808.RegisterResp;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.enums.TransStrategyEnum;
import com.yunji.hygiene.service.DeviceCallService;
import com.yunji.hygiene.service.DeviceFileCache;
import com.yunji.hygiene.service.SystemUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 注册消息->RegisterResp
 * @Version: 1.0
 */

@Slf4j
@Component(HandleConstant.REGISTER_SERVICE)
@ChannelHandler.Sharable
public class RegisterMsgHandler extends AbsChannelReadHandler<RegisterMsg> {

    @Resource
    private DeviceCallService deviceCallService;

    @Override
    protected void readData(ChannelHandlerContext ctx, DataPacket msg) {
        String imei = ChannelManager.getImei(ctx.channel());
        deviceService.cabinetOnline(imei);
        if (SystemUtil.redisCache().hasKey(DeviceCacheCode.DEVICE_UPGRADE_TASK + imei)) {
            UpGradeFileDTO info = DeviceFileCache.getInfo(imei);
            if (info != null)
                deviceCallService.command(new UpgradeCommandDTO(TransStrategyEnum.DEVICE_GRADE.name(), -1, imei
                        , info.getFileId(), info.getInfoId()));
        }
        //默认注册成功
        RegisterResp resp = RegisterResp.success(msg, getSerialNumber(ctx.channel()));
        log.info("RegisterMsgHandler readData,imei:{} ", msg.getHeader().getImei());
        write(ctx, resp);
    }
}
