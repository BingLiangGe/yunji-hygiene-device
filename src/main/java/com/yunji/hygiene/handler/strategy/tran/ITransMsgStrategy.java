package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.dto.HygieneCommandDTO;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.util.SpringUtils;

/**
 * @author : peter-zhu
 * @date : 2025/1/14 14:25
 * @description : TODO
 **/
public interface ITransMsgStrategy {
    TransMsg strategyTranMsg(HygieneCommandDTO cmd);

    default DeviceService deviceService(){
        return SpringUtils.getBean(DeviceService.class);
    }
}
