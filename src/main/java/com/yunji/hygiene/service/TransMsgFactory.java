package com.yunji.hygiene.service;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author : peter-zhu
 * @date : 2025/1/14 11:39
 * @description : 本来想用工厂模式 发现有局限性 改为策略 先不删除留着
 **/
@Component
public class TransMsgFactory {
    private static final Map<String, Supplier<TransMsg>> msgCreators = new HashMap<>();

    private TransMsgFactory() {
    }

    @PostConstruct
    private void init() {
    }

    public static TransMsg create(String command) {
        Supplier<TransMsg> creator = msgCreators.get(command);
        if (creator != null) {
            return creator.get();
        }
        throw new IllegalArgumentException("No such command: " + command);
    }
}
