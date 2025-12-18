package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * PingStrategy：设备探活/心跳指令策略（下发 PING）
 *
 * 作用：
 * - 构造一个“空包体”的透传消息（EmptyTransMsg）
 * - messageType = PING
 * - 用于后台主动探测设备是否在线/唤醒设备等场景
 *
 * 典型链路：
 * 业务触发 ping -> 获取 PingStrategy -> 构造 TransMsg -> 封装 JT808 8900 -> 编码 -> writeAndFlush
 */
public class PingStrategy implements ITransMsgStrategy {

    /**
     * 构造 PING 透传消息
     *
     * @param cmd 命令入参（该策略不依赖 cmd 中字段，保留是为了策略接口统一）
     * @return 透传消息体（EmptyTransMsg），后续会被统一封装并通过 TCP 下发给设备
     */
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {

        // 1）PING 不需要额外参数，所以使用 EmptyTransMsg（空包体）
        EmptyTransMsg pingMsg = new EmptyTransMsg();

        // 2）eventId = -1：表示不绑定业务事件（不需要事件回写/跟踪）
        pingMsg.setEventId(-1);

        // 3）设置透传类型：PING（设备端收到后一般会回 PONG 或上报状态）
        pingMsg.setMessageType(TransEnum.PING.getIssueType());

        // 4）返回透传消息体
        return pingMsg;
    }
}
