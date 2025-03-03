package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.OperateTransMsg;
import com.yunji.hygiene.entity.dto.HygieneCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * @author : peter-zhu
 * @date : 2025/1/14 14:51
 * @description : TODO
 **/
public class OpenShippingStrategy  implements ITransMsgStrategy {
    @Override
    public TransMsg strategyTranMsg(HygieneCommandDTO cmd) {
        OperateTransMsg rs = new OperateTransMsg();
        rs.setMessageType(TransEnum.SHIPPING.getIssueType());
        rs.setOpenNum((byte) 1);
        rs.setAction((byte) 0);
        return rs;
    }
}
