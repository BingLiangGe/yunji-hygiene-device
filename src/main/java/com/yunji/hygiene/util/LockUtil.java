package com.yunji.hygiene.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 *
 * @author Blue.lan
 * @since 2022-06-24
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class LockUtil implements Closeable {
    private static RedissonClient redisson;

    private RLock current;

    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        LockUtil.redisTemplate = redisTemplate;
    }

    public static boolean lockWithoutThread(String key,
                                            long waitTime, long leaseTime, TimeUnit timeUnit) {
        return lockWithoutThread(key, key, waitTime, leaseTime, timeUnit, 1000);
    }

    public static boolean lockWithoutThread(String key, String value,
                                            long waitTime, long leaseTime, TimeUnit timeUnit, long sleepWait) {
        long startTime = System.currentTimeMillis();
        long waitMillis = timeUnit.toMillis(waitTime);
        long leaseMillis = timeUnit.toMillis(leaseTime);
        while ((System.currentTimeMillis() - startTime) < waitMillis) {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, leaseMillis, timeUnit);
            if (Boolean.TRUE.equals(success)) {
                return true;
            }
            try {
                Thread.sleep(sleepWait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return false;
    }

    public static boolean unLockWithoutThread(String key) {
        return unLockWithoutThread(key, key);
    }

    public static boolean unLockWithoutThread(String key, String value) {
        String currentValue = (String) redisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    /**
     * 根据名称加锁, 拿不到锁就不罢休, 不然线程就一直block（会自动续期）
     *
     * @param key 名称
     * @return 锁实例
     */
    public static void lock(String key) {
        RLock lock = redisson.getLock(key);
        lock.lock();
    }

    /**
     * 使用定义的租赁时间获取锁, 锁定将在定义的租赁时间间隔后自动解除（不会自动续期）
     *
     * @param key  锁名称
     * @param time 租赁时间, 单位为秒
     * @return 锁实例
     */
    public static void lock(String key, long time) {
        lock(key, time, TimeUnit.SECONDS);
    }

    /**
     * 使用定义的租赁时间获取锁, 锁定将在定义的租赁时间间隔后自动解除
     *
     * @param key  锁名称
     * @param time 租赁时间
     * @param unit 时间单位
     * @return 锁实例
     */
    public static void lock(String key, long time, TimeUnit unit) {
        RLock lock = redisson.getLock(key);
        lock.lock(time, unit);
    }

    public static boolean tryLock(String key, long wait, long lease) {
        return tryLock(key, wait, lease, TimeUnit.SECONDS);
    }

    public static boolean tryLock(String key, long wait) {
        return tryLock(key, wait, 0L, TimeUnit.SECONDS);
    }

    /**
     * 尝试使用定义的租赁时间获取锁。
     *
     * @param key   锁名称
     * @param wait  等待时间
     * @param lease 租赁时间
     * @param unit  时间单位
     * @return 是否加上锁
     */
    public static boolean tryLock(String key, long wait, long lease, TimeUnit unit) {
        try {
            RLock lock = redisson.getLock(key);
            return lock.tryLock(wait, lease, unit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 通过名称解锁
     *
     * @param key 锁名称
     */
    public static void unlock(String key) {
        RLock lock = redisson.getLock(key);
        lock.unlock();
    }

    public static LockUtil create(String key) {
        LockUtil util = new LockUtil();
        util.current = redisson.getLock(key);
        util.current.lock();
        return util;
    }

    @Override
    public void close() throws IOException {
        current.unlock();
    }

    @Autowired
    public void setRedisson(RedissonClient redisson) {
//        Config config = new Config(redisson.getConfig());
//        config.useSingleServer().setDatabase(8);
        LockUtil.redisson = redisson;
    }

}
