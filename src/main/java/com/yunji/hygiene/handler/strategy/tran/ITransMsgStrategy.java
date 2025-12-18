package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.util.SpringUtils;

/**
 * ITransMsgStrategy：透传指令“策略接口”
 *
 * 设计目的：
 * - 把“不同业务命令 -> 构造不同透传消息体(TransMsg)”的逻辑解耦出来
 * - 上层只需要根据 command 找到策略，然后调用 strategyTranMsg(cmd) 得到 TransMsg
 * - 后续统一由 Netty/JT808 层封装成 8900 报文并下发
 *
 * 典型使用：
 * ITransMsgStrategy strategy = TransMsgStrategyFactory.getStrategy(command);
 * TransMsg transMsg = strategy.strategyTranMsg(enterCommandDTO);
 * DataPacket packet = AbsChannelReadHandler.convertTransMsg(eventId, channel, imei, transMsg);
 * channel.writeAndFlush(packet);
 *
 * 为什么接口里提供 deviceService() 这个默认方法？
 * - 你们目前的策略对象（OpenRestockStrategy / UpgradeStrategy 等）是通过 new 创建的，
 *   不是 Spring 容器管理的 Bean（见 TransMsgStrategyFactory static { new XxxStrategy(); }）
 * - 不是 Spring Bean 就无法使用 @Autowired/@Resource 注入 DeviceService
 * - 但某些策略（如升级策略）确实需要查数据库/查缓存/写事件表等能力
 * - 因此通过 SpringUtils.getBean(DeviceService.class) “按需从 Spring 容器取 Bean”
 *   来解决“非 Spring 管理对象也想使用 Spring Bean”的问题
 *
 * 这种写法的利弊（你们为什么要这么用）：
 * ✅ 优点：
 * 1）策略类可以保持简单：new 出来就能用，不依赖 Spring 注入
 * 2）策略工厂静态注册，获取速度快、结构清晰
 * 3）确实能解决“策略不是 Bean，但要用 Service”的现实问题
 *
 * ⚠️缺点/风险（要知道但不一定马上改）：
 * 1）隐藏依赖：strategyTranMsg() 表面只要 cmd，实际可能还会依赖 deviceService()
 * 2）不利于单元测试：SpringUtils.getBean 让测试必须启动 Spring 容器或做复杂 Mock
 * 3）全局静态取 Bean 容易被滥用，后期维护成本会上升
 *
 * 如果以后想更标准：
 * - 可以把策略都交给 Spring 管理（@Component），工厂从 ApplicationContext 注入 Map<String, ITransMsgStrategy>
 * - 或者构造器注入 DeviceService，再由工厂注入/装配策略实例
 */
public interface ITransMsgStrategy {

    /**
     * 构造透传消息体（TransMsg）
     *
     * @param cmd 业务侧命令参数（如：imei、出货列表、升级参数等）
     * @return 透传消息体（后续会被封装进 JT808 8900 报文）
     */
    TransMsg strategyTranMsg(EnterCommandDTO cmd);

    /**
     * 兜底获取 DeviceService（按需从 Spring 容器取）
     *
     * 为什么放在接口 default 方法里？
     * - 所有策略都会继承这个接口
     * - 需要用到 DeviceService 的策略可以直接 deviceService().xxx()
     * - 不需要的策略不受影响（不用管注入问题）
     *
     * 适用前提：
     * - SpringUtils 必须能拿到 ApplicationContext（项目启动时已注入）
     * - DeviceService 必须是 Spring Bean（@Service）
     */
    default DeviceService deviceService() {
        return SpringUtils.getBean(DeviceService.class);
    }
}
