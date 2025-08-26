package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

public class ResetStrategy implements ITransMsgStrategy{
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {
        EmptyTransMsg getDeviceInfo = new EmptyTransMsg();
        getDeviceInfo.setEventId(-1);
        getDeviceInfo.setMessageType(TransEnum.DEVICE_RESET.getIssueType());
        return getDeviceInfo;
    }
}
