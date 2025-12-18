package com.yunji.hygiene.service;

import com.yunji.hygiene.config.RedisCache;
import com.yunji.hygiene.constant.CacheCode;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author: peter-zhu
 * @Date: 2024/10/22 14:50
 * @Description: 系统工具类，提供与 Redis 相关的功能，特别是获取递增的唯一键。
 * @Version: 1.0
 */
@Component
public class SystemUtil {

    /**
     * Redis 缓存对象，用于操作 Redis 缓存
     */
    public static RedisCache redisCache;

    /**
     * Redisson 客户端，用于操作 Redis 中的原子操作等
     */
    public static RedissonClient redisson;

    /**
     * 注入 Redisson 客户端实例
     * @param redisson Redisson 客户端实例
     */
    @Autowired
    protected void setRedisson(RedissonClient redisson) {
        SystemUtil.redisson = redisson;
    }

    /**
     * 注入 Redis 缓存实例
     * @param redisCache Redis 缓存实例
     */
    @Autowired
    protected void setRedisCache(RedisCache redisCache) {
        SystemUtil.redisCache = redisCache;
    }

    /**
     * 获取 Redis 缓存实例
     * @return Redis 缓存实例
     */
    public static RedisCache redisCache() {
        return redisCache;
    }

    /**
     * 获取递增的唯一键，默认 60 秒过期
     * @param key 键名
     * @return 增加后的值
     */
    public static Long getIncrementKey(String key) {
        return getIncrementKey(key, 60, TimeUnit.SECONDS); // 默认 60 秒过期
    }

    /**
     * 获取递增的唯一键，可以设置过期时间
     * @param key 键名
     * @param time 过期时间
     * @param timeUnit 时间单位
     * @return 增加后的值
     */
    public static Long getIncrementKey(String key, Integer time, TimeUnit timeUnit) {
        // 获取 Redisson 中的原子长整型
        RAtomicLong atomicLong = redisson.getAtomicLong(CacheCode.NUM_INCREMENT + key);

        // 设置键的过期时间
        atomicLong.expire(time, timeUnit);

        // 返回递增后的值
        return atomicLong.incrementAndGet();
    }
}
