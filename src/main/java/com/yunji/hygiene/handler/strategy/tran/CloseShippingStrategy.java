package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.OperateTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * @author : peter-zhu
 * @date : 2025/1/22 19:48
 * @description : TODO
 **/
public class CloseShippingStrategy implements ITransMsgStrategy{
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {
        OperateTransMsg rs = new OperateTransMsg();
        rs.setMessageType(TransEnum.SHOPPING.getIssueType());
        rs.setOpenNum((byte) 1);
        rs.setAction((byte) 0);
        return rs;
    }
}
