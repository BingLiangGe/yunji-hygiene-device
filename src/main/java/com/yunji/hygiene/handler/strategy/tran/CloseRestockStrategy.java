package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.OperateTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * CloseRestockStrategy：关闭补货门/补货舱的透传指令策略
 * - 构造 OperateTransMsg（操作类消息）
 * - messageType=RESTOCK（补货相关操作）
 * - openNum=1（默认操作第1个补货门/舱）
 * - action=1（关闭/打开含义以设备协议为准，这里按“关闭”来用）
 */
public class CloseRestockStrategy implements ITransMsgStrategy {

    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {
        // 操作类透传消息（用于开/关补货门等动作）
        OperateTransMsg rs = new OperateTransMsg();

        // 补货相关操作类型
        rs.setMessageType(TransEnum.RESTOCK.getIssueType());

        // 要操作的门/舱编号（这里固定为 1）
        rs.setOpenNum((byte) 1);

        // 动作值（0/1 含义以设备协议约定为准；这里用 1 表示关闭）
        rs.setAction((byte) 1);

        return rs;
    }
}
