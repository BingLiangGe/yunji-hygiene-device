package com.yunji.hygiene.service;

import com.yunji.hygiene.config.RedisCache;
import com.yunji.hygiene.constant.CacheCode;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * @author : peter-zhu
 * @date : 2024/10/22 14:50
 * @description : TODO
 **/
@Component
public class SystemUtil {

    public static RedisCache redisCache;
    public static RedissonClient redisson;

    @Autowired
    protected void setRedisson(RedissonClient redisson) {
        SystemUtil.redisson = redisson;
    }

    @Autowired
    protected void setRedisCache(RedisCache redisCache) {
        SystemUtil.redisCache = redisCache;
    }

    public static RedisCache redisCache() {
        return redisCache;
    }

    public static Long getIncrementKey(String key) {
        return getIncrementKey(key, 60, TimeUnit.SECONDS);
    }

    public static Long getIncrementKey(String key, Integer time, TimeUnit timeUnit) {
        RAtomicLong atomicLong = redisson.getAtomicLong(CacheCode.NUM_INCREMENT + key);
        atomicLong.expire(time, timeUnit);
        return atomicLong.incrementAndGet();
    }
}
