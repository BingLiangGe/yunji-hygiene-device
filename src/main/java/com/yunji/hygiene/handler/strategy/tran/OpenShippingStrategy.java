package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.PurchaseTransMsg;
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
        PurchaseTransMsg rs = new PurchaseTransMsg();
        rs.setMessageType(TransEnum.SHOPPING.getIssueType());
        rs.setPairList(cmd.getPurchase());
        return rs;
    }
}
