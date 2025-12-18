package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * ResetStrategy：设备重启/复位指令策略
 *
 * 作用：
 * - 构造一个“空包体”的透传消息（EmptyTransMsg）
 * - 指定 messageType 为 DEVICE_RESET
 * - 由上层统一封装为 JT808 8900 下发给设备
 *
 * 特点：
 * - 该指令不需要额外参数，因此使用 EmptyTransMsg（只有 eventId / messageType 等基础字段）
 */
public class ResetStrategy implements ITransMsgStrategy {

    /**
     * 根据业务命令参数构造“设备复位”透传消息
     *
     * @param cmd 命令入参（当前策略不使用 cmd 内字段；保留是为了接口统一）
     * @return 透传消息体（EmptyTransMsg），将被进一步封装并通过 TCP 下发给设备
     */
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {

        // 1）构造空透传消息（无 contentBytes）
        EmptyTransMsg resetMsg = new EmptyTransMsg();

        // 2）eventId = -1：表示不关联具体业务事件/不需要回写事件表
        resetMsg.setEventId(-1);

        // 3）设置透传消息类型：设备重启/复位
        // TransEnum.DEVICE_RESET.getIssueType() 会返回设备端识别的“消息类型字节”
        resetMsg.setMessageType(TransEnum.DEVICE_RESET.getIssueType());

        // 4）返回消息体，后续由 convertTransMsg + Encoder 编码下发
        return resetMsg;
    }
}
