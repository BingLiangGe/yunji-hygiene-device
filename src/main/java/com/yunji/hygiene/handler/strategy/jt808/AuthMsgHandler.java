package com.yunji.hygiene.handler.strategy.jt808;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.config.RedisCache;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.constant.DeviceLockCode;
import com.yunji.hygiene.constant.HandleConstant;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.req.jt808.AuthMsg;
import com.yunji.hygiene.entity.domain.resp.jt808.CommonResp;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.enums.TransStrategyEnum;
import com.yunji.hygiene.service.DeviceCallService;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

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
        ChannelManager.add(imei, ctx.channel());
        CommonResp resp = CommonResp.success(msg, getSerialNumber(ctx.channel()));
        log.info("AuthMsgHandler readData,imei:{} ", imei);
        RedisCache redisCache = SystemUtil.redisCache();
        List<String> imeiInfoIdKeys = redisCache.scanPattern(DeviceCacheCode.DEVICE_UPGRADE_TASK + imei + ":*");
        if (!CollectionUtils.isEmpty(imeiInfoIdKeys) &&  !redisCache.hasKey(DeviceLockCode.CABINET_UPGRADE_LOCK + imei)) {
            log.info("AuthMsgHandler readData scanPattern imei:{} ", imeiInfoIdKeys);
            String maxImeiInfoIdKey = imeiInfoIdKeys.stream().max(Comparator.naturalOrder()).orElse(null);
            if (maxImeiInfoIdKey != null) {
                for (String imeiInfoIdKey : imeiInfoIdKeys) {
                    if (!imeiInfoIdKey.equals(maxImeiInfoIdKey))
                        redisCache.delete(imeiInfoIdKey);
                }
                String replace = maxImeiInfoIdKey.replace(DeviceCacheCode.DEVICE_UPGRADE_TASK + imei + ":", "");
                Long infoId = Long.parseLong(replace);
                Long fileId = deviceService.getFileByInfoId(infoId);
                UpGradeFileDTO info = deviceService.createUpgradeCache(imei, infoId, fileId);
                log.info("AuthMsgHandler hasKey DEVICE_UPGRADE_TASK start upgrade,imei:{} info:{}", imei, JsonUtil.toJsonString(info));
                if (info != null)
                    deviceCallService.command(new UpgradeCommandDTO(TransStrategyEnum.DEVICE_GRADE.name(), -1, imei
                            , info.getFileId(), info.getInfoId()));
            }
        }
        deviceService.cabinetOnline(imei);
        write(ctx, resp);
    }
}
