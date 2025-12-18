package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.enums.TransStrategyEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * TransMsgStrategyFactory：透传指令策略工厂（命令 -> 策略实现）
 *
 * 作用：
 * - 根据业务命令（command）找到对应的 ITransMsgStrategy 实现
 * - 不同策略负责构造不同的“透传消息体（TransMsg）”，最终会被封装成 JT808 8900 下发给设备
 *
 * 典型链路：
 * Controller/Service 发起命令 -> 根据 command 获取策略 -> strategyTranMsg() 构造 TransMsg -> AbsChannelReadHandler.convertTransMsg() 封装 -> Encoder 编码 -> 写入 TCP
 *
 * 注意点：
 * - 当前用的是 static + new 的方式注册策略（非 Spring Bean）
 *   1）优点：简单直接，无需注入
 *   2）缺点：策略里如果需要 @Resource/@Autowired 注入将失效（因为不是 Spring 管理）
 *      比如 UpgradeStrategy 里如果未来要注入 DeviceService，会拿不到
 */
public class TransMsgStrategyFactory {

    /**
     * 策略容器：key=命令名（TransStrategyEnum.name()），value=对应策略实现
     *
     * 示例：
     * - OPEN_RESTOCK  -> OpenRestockStrategy
     * - PING         -> PingStrategy
     */
    private static final Map<String, ITransMsgStrategy> strategies = new HashMap<>();

    /**
     * 静态初始化：系统启动时把所有策略注册到 map
     * - 后续 getStrategy(command) 就能 O(1) 取到策略
     */
    static {
        strategies.put(TransStrategyEnum.OPEN_RESTOCK.name(), new OpenRestockStrategy());     // 开补货门
        strategies.put(TransStrategyEnum.OPEN_SHIPPING.name(), new OpenShippingStrategy());   // 开出货门
        strategies.put(TransStrategyEnum.CLOSE_RESTOCK.name(), new CloseRestockStrategy());   // 关补货门
        strategies.put(TransStrategyEnum.CLOSE_SHIPPING.name(), new CloseShippingStrategy()); // 关出货门
        strategies.put(TransStrategyEnum.PING.name(), new PingStrategy());                    // 心跳/探活
        strategies.put(TransStrategyEnum.GET_VERSION.name(), new GetVersionStrategy());       // 查询版本
        strategies.put(TransStrategyEnum.DEVICE_RESET.name(), new ResetStrategy());           // 设备重启/复位
        strategies.put(TransStrategyEnum.DEVICE_GRADE.name(), new UpgradeStrategy());         // OTA升级准备/升级流程入口
    }

    /**
     * 获取策略
     *
     * @param command 命令字符串（建议使用 TransStrategyEnum.name() 保持一致）
     * @return 对应策略实现；若返回 null，表示没有注册该命令（上层应做兜底处理）
     */
    public static ITransMsgStrategy getStrategy(String command) {
        return strategies.get(command);
    }
}
