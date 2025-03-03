package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.dto.HygieneCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * @author : peter-zhu
 * @date : 2025/1/24 14:23
 * @description : TODO
 **/
public class PingStrategy implements ITransMsgStrategy {
    @Override
    public TransMsg strategyTranMsg(HygieneCommandDTO cmd) {
        EmptyTransMsg getDeviceInfo = new EmptyTransMsg();
        getDeviceInfo.setEventId(-1);
        getDeviceInfo.setMessageType(TransEnum.PING.getIssueType());
        return getDeviceInfo;
    }
}
