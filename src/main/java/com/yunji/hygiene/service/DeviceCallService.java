package com.yunji.hygiene.service;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.DeviceException;
import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.enums.DeviceErrorEnum;
import com.yunji.hygiene.entity.enums.TransEnum;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import com.yunji.hygiene.handler.strategy.tran.ITransMsgStrategy;
import com.yunji.hygiene.handler.strategy.tran.TransMsgStrategyFactory;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 设备控制服务类，提供了设备相关的指令发送、状态检测、升级等操作。
 * 主要用于设备通信命令的发送与接收。
 */
@Service
@Slf4j
public class DeviceCallService {

    // 注入设备服务
    @Resource
    private DeviceService deviceService;

    /**
     * 发送心跳包（Ping）给设备，并根据返回情况处理设备状态
     * @param imei 设备唯一标识码
     * @param reportStatus 是否上报状态
     * @return 返回命令发送是否成功
     */
    public boolean ping(String imei, boolean reportStatus) {
        // 构建心跳指令
        boolean ack = command(new EnterCommandDTO(TransEnum.PING.getCmd(), reportStatus ? 0 : -1, imei));
        if (!ack)
            // 如果心跳失败，标记设备为离线
            deviceService.cabinetOffline(imei);
        return ack;
    }

    /**
     * 重置设备，发送设备重置指令
     * @param imei 设备唯一标识码
     * @return 返回指令是否成功
     */
    public boolean reset(String imei) {
        return command(new EnterCommandDTO(TransEnum.DEVICE_RESET.getCmd(), -1, imei));
    }

    /**
     * 进行缓存文件升级操作，首先将升级缓存创建成功后，再发送升级指令
     * @param dto 升级指令数据
     * @return 返回是否成功
     */
    public boolean cacheFileUpgrade(UpgradeCommandDTO dto) {
        // 创建升级缓存
        boolean upgradeCache = deviceService.createUpgradeCache(dto);
        if (!upgradeCache) {
            log.error("cacheFileUpgrade error:{}", JsonUtil.toJsonString(dto));
            return false;
        }
        // 执行指令发送
        return command(dto);
    }

    /**
     * 发送指令到设备
     * @param dto 指令数据
     * @return 返回是否成功
     */
    public boolean command(EnterCommandDTO dto) {
        try {
            log.info("DeviceCallService.command({})", dto);
            String imei = dto.getImei();
            // 获取设备对应的通道
            Channel channel = ChannelManager.getChannel(imei);
            if (channel == null)
                // 如果没有找到设备通道，抛出设备异常
                throw new DeviceException(DeviceErrorEnum.CHECKED_202503, dto.getImei());

            // 获取对应的策略
            ITransMsgStrategy strategy = TransMsgStrategyFactory.getStrategy(dto.getCmd());
            if (strategy == null)
                // 如果没有找到匹配的策略，抛出设备异常
                throw new DeviceException(DeviceErrorEnum.CHECKED_202502, dto.getCmd(), dto.getImei());

            // 使用策略生成对应的指令消息
            TransMsg transMsg = strategy.strategyTranMsg(dto);
            if (transMsg == null)
                return false;

            // 如果事件ID为-1，保持该状态
            if (transMsg.getEventId() == -1)
                dto.setEventId(-1);

            // 将指令转换为数据包，准备发送
            DataPacket packet = AbsChannelReadHandler.convertTransMsg(dto.getEventId(), channel, imei, transMsg);
            log.debug("DeviceCallService command packet:{}", JsonUtil.toJsonString(packet));

            // 异步发送指令并添加监听
            ChannelFuture channelFuture = channel.writeAndFlush(packet);
            channelFuture.addListener(future -> {
                if (future.isSuccess())
                    log.info("下发指令返回成功:{}", dto);
                else
                    log.error("下发指令返回失败,请求数据{}.原因:｛｝", dto, future.cause());
            });

            try {
                // 等待指令发送完成并获取结果
                boolean completed = channelFuture.await(10, TimeUnit.SECONDS);
                if (completed) {
                    boolean success = channelFuture.isSuccess();
                    log.info("imei: {}, 下发指令ACK: {}", imei, success);
                    return success;
                } else {
                    log.error("下发指令超时, 数据: {}", dto);
                    return false;
                }
            } catch (InterruptedException e) {
                // 如果在等待过程中被中断，记录日志并返回失败
                log.error("下发指令等待操作完成时被中断,数据:{}", dto, e);
                Thread.currentThread().interrupt();
                return false;
            }
        } catch (Exception e) {
            if (e instanceof DeviceException) {
                // 如果是设备异常，抛出自定义设备异常
                throw new DeviceException(DeviceErrorEnum.CHECKED_202512, dto.getImei() + ",描述：" + e.getMessage());
            }
            throw e; // 其他异常继续抛出
        }
    }
}
