package com.yunji.hygiene.service;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 负责创建不同类型的 TransMsg 消息对象的工厂类。使用策略模式来根据传入的命令创建相应的 TransMsg 实例。
 * 原本考虑使用工厂模式，但为了灵活扩展，转而采用策略模式来应对不同的命令类型。
 *
 * 该类的作用是根据不同的命令（command）动态创建对应的 TransMsg 对象。
 * 在命令与消息的映射关系发生变化时，只需调整 msgCreators 的配置，避免修改代码中其他部分。
 *
 * @author : peter-zhu
 * @date : 2025/1/14 11:39
 */
@Component
public class TransMsgFactory {

    // 存储命令与 TransMsg 创建器的映射关系
    private static final Map<String, Supplier<TransMsg>> msgCreators = new HashMap<>();

    // 构造函数为私有的，避免外部直接实例化
    private TransMsgFactory() {
    }

    /**
     * 初始化方法，在类实例化后执行，负责配置命令与 TransMsg 创建器的映射关系。
     * 目前该方法是空的，但在以后可以用于注册命令和对应的消息创建器。
     */
    @PostConstruct
    private void init() {
        // 可以在此方法中注册不同命令的创建器，如：
        // msgCreators.put("command1", () -> new TransMsg1());
        // msgCreators.put("command2", () -> new TransMsg2());
        // 此处的映射关系可以根据实际需求添加
    }

    /**
     * 根据命令字符串创建对应的 TransMsg 消息对象。
     *
     * @param command 命令字符串，用于查找对应的消息创建器
     * @return 根据命令创建的 TransMsg 对象
     * @throws IllegalArgumentException 如果未找到对应的命令，抛出该异常
     */
    public static TransMsg create(String command) {
        // 查找命令对应的创建器
        Supplier<TransMsg> creator = msgCreators.get(command);

        // 如果找到对应的创建器，使用它创建 TransMsg 对象
        if (creator != null) {
            return creator.get();
        }

        // 如果未找到对应的命令，抛出异常
        throw new IllegalArgumentException("No such command: " + command);
    }
}
