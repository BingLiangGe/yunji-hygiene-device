package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * @author : peter-zhu
 * @date : 2025/2/10 14:56
 * @description : TODO
 **/
public class GetVersionStrategy implements ITransMsgStrategy {
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {
        EmptyTransMsg getDeviceInfo = new EmptyTransMsg();
        getDeviceInfo.setEventId(-1);
        getDeviceInfo.setMessageType(TransEnum.GET_VERSION.getIssueType());
        return getDeviceInfo;
    }
}
