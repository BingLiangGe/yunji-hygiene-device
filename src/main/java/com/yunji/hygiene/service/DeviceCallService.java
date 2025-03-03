package com.yunji.hygiene.service;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.DeviceException;
import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.dto.HygieneCommandDTO;
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

import java.util.concurrent.TimeUnit;

/**
 * @author : peter-zhu
 * @date : 2025/1/9 17:37
 * @description : TODO
 **/
@Service
@Slf4j
public class DeviceCallService {

    public boolean ping(String imei, boolean reportStatus) {
        return command(new HygieneCommandDTO(TransEnum.PING.getCmd(), reportStatus ? 0 : -1, imei));
    }

    public boolean command(HygieneCommandDTO dto) {
        try {
            String imei = dto.getImei();
            Channel channel = ChannelManager.getChannel(imei);
            if (channel == null)
                throw new DeviceException(DeviceErrorEnum.CHECKED_202503, dto.getImei());
            ITransMsgStrategy strategy = TransMsgStrategyFactory.getStrategy(dto.getCmd());
            if (strategy == null)
                throw new DeviceException(DeviceErrorEnum.CHECKED_202502, dto.getCmd(), dto.getImei());
            TransMsg transMsg = strategy.strategyTranMsg(dto);
            if (transMsg.getEventId() == -1)
                dto.setEventId(-1);
            DataPacket packet = AbsChannelReadHandler.convertTransMsg(dto.getEventId(), channel, imei, transMsg);
            log.debug("DeviceCallService command packet:{}", JsonUtil.toJsonString(packet));
            ChannelFuture channelFuture = channel.writeAndFlush(packet);
            channelFuture.addListener(future -> {
                if (future.isSuccess())
                    log.info("下发指令返回成功:{}", dto);
                else
                    log.error("下发指令返回失败,请求数据{}.原因:｛｝", dto, future.cause());
            });

            try {
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
                log.error("下发指令等待操作完成时被中断,数据:{}", dto, e);
                Thread.currentThread().interrupt();
                return false;
            }
        } catch (Exception e) {
            if (e instanceof DeviceException) {
                throw new DeviceException(DeviceErrorEnum.CHECKED_202512, dto.getImei() + ",描述：" + e.getMessage());
            }
            throw e;
        }
    }

    //        ChannelManager.onlineException(cmd.getImei());
//        BaseHandler<?> baseHandler = strategyMap.get(HandleConstant.ISSUE_SERVICE);
//        return baseHandler.handle(cmd);

    //        TransEnum cmdEnum = TransEnum.getIssueEnumByCmd(dto.getCmd());
//        if (cmdEnum == null)
//            throw new DeviceException(ErrorEnum.CHECKED_202502, dto.getCmd(), dto.getImei());
    //        DataPacket.Header header = new DataPacket.Header();r
//        header.setMsgId(JT808Const.TERMINAL_MSG_ISSUE);
//        header.setFlowId(getSerialNumber(channel));
//        header.setImei(imei);
//        header.setMsgBodyProps((short) 0);
//        IssueMsg issueMsg = new OpenIssueMsg();
//        issueMsg.setHeader(header);
//        issueMsg.setMessageType(cmdEnum.getIssueType());
//        issueMsg.setPackageLength((short) 2);
//        issueMsg.setMessageLength((short) 2);
//        ByteBuf byteBuf = Unpooled.buffer(9);
//        byte[] data = new byte[]{0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x02, 0x01, 0x01};
//        byteBuf.writeBytes(data);
//        issueMsg.setPayload(byteBuf);
//        AtomicBoolean flag = new AtomicBoolean(false);
//        ChannelFuture channelFuture = channel.writeAndFlush(issueMsg);
    //AtomicBoolean flag = new AtomicBoolean(false);

//    public boolean handle(HygieneHandleDTO handle) {
//        ChannelManager.onlineException(handle.getImei());
//        BaseHandler<?> baseHandler = strategyMap.get(handle.getServiceHandle());
//        if (baseHandler == null)
//            throw new DeviceException(DeviceErrorEnum.CHECKED_202501, handle.getServiceHandle(), handle.getImei());
//        return baseHandler.handle(handle);
//    }
}
