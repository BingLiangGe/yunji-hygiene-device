package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.OperateTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * CloseShippingStrategy：关闭出货门/出货舱的透传指令策略
 * - 构造 OperateTransMsg（操作类消息）
 * - messageType=SHOPPING（出货相关操作）
 * - openNum=1（默认操作第1个出货门/舱）
 * - action=0（关闭/打开含义以设备协议为准）
 */
public class CloseShippingStrategy implements ITransMsgStrategy {

    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {
        // 操作类透传消息（用于开/关门等动作）
        OperateTransMsg rs = new OperateTransMsg();

        // 出货相关操作类型（设备端根据该类型执行对应动作）
        rs.setMessageType(TransEnum.SHOPPING.getIssueType());

        // 要操作的门/舱编号（这里固定为 1）
        rs.setOpenNum((byte) 1);

        // 动作值（0/1 含义请以设备端协议约定为准）
        rs.setAction((byte) 0);

        return rs;
    }
}
