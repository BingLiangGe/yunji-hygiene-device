package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.OperateTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * OpenRestockStrategy：开启补货门/补货舱 指令策略
 *
 * 作用：
 * - 构造 OperateTransMsg（操作类透传消息）
 * - messageType 设置为 RESTOCK（设备端识别为“补货相关操作”）
 * - openNum / action 组合描述具体动作（这里固定为：打开补货门）
 *
 * 一般约定（以你们设备协议为准）：
 * - openNum：要操作的“门/舱”编号或数量（例如 1 表示补货门）
 * - action：动作类型（例如 0=打开，1=关闭 或相反，需与固件协议一致）
 *
 * 注意：
 * - 该策略只负责拼装透传消息体，真正下发需要上层封装 JT808 8900 并编码发送
 */
public class OpenRestockStrategy implements ITransMsgStrategy {

    /**
     * 构造“打开补货门”透传消息
     *
     * @param cmd 命令入参（该策略当前不使用 cmd，保留是为了策略接口统一）
     * @return OperateTransMsg（继承 TransMsg）
     */
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {

        // 1）构造操作类透传消息（用于开门/关门等“操作指令”）
        OperateTransMsg rs = new OperateTransMsg();

        // 2）设置透传消息类型：RESTOCK（补货操作大类）
        rs.setMessageType(TransEnum.RESTOCK.getIssueType());

        // 3）openNum：补货门编号/数量（这里固定 1，表示“补货门/补货舱”）
        rs.setOpenNum((byte) 1);

        // 4）action：动作（这里固定 0，表示“打开”——具体 0/1 含义以设备协议为准）
        rs.setAction((byte) 0);

        // 5）返回消息体
        return rs;
    }
}
