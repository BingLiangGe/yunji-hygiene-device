package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.enums.TransStrategyEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : peter-zhu
 * @date : 2025/1/14 14:48
 * @description : TODO
 **/
public class TransMsgStrategyFactory {

    private static final Map<String, ITransMsgStrategy> strategies = new HashMap<>();


    static {
        strategies.put(TransStrategyEnum.OPEN_RESTOCK.name(), new OpenRestockStrategy());
        strategies.put(TransStrategyEnum.OPEN_SHIPPING.name(), new OpenShippingStrategy());
        strategies.put(TransStrategyEnum.CLOSE_RESTOCK.name(), new CloseRestockStrategy());
        strategies.put(TransStrategyEnum.CLOSE_SHIPPING.name(), new CloseShippingStrategy());
        strategies.put(TransStrategyEnum.PING.name(), new PingStrategy());
        strategies.put(TransStrategyEnum.GET_VERSION.name(), new GetVersionStrategy());
        strategies.put(TransStrategyEnum.DEVICE_GRADE.name(), new UpgradeStrategy());
    }

    public static ITransMsgStrategy getStrategy(String command) {
        return strategies.get(command);
    }
}
