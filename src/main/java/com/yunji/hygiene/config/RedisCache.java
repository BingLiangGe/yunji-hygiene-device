package com.yunji.hygiene.config;

import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Project:    yunji-base-mall
 * @Package:    com.yunji.core.utils.redis
 * @Title:      RedisCache
 * @Description:
 * Spring Redis工具类
 * History:
 * Date                     Version     Author          Summary
 * ===================================================================
 * 2024-03-07 14:14:40      V1.0        HuaAo       新建类
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Resource
    public RedisTemplate redisTemplate;

    /**
     * @Title	set
     * @Desc	缓存基本的对象，Integer、String、实体类等
     * @Date	2024-03-23 10:26:08
     * @Param	k 缓存的键值
     * @Param	v 缓存的值
     */
    public <T> void set(final String k, final T v)
    {
        redisTemplate.opsForValue().set(k, v);
    }

    /**
     * @Title	set
     * @Desc	缓存基本的对象，Integer、String、实体类等
     * @Date	2024-03-23 10:28:37
     * @Param	k 缓存的键值
     * @Param	v 缓存的值
     * @Param	timeout 时间
     * @Param	unit 时间颗粒度
     */
    public <T> void set(final String k, final T v, final Integer timeout, final TimeUnit unit)
    {
        redisTemplate.opsForValue().set(k, v, timeout, unit);
    }

    /**
     * @Title	get
     * @Desc	获得缓存的基本对象
     * @Date	2024-03-23 10:34:07
     * @Author	HuaAo
     * @Param	k Redis键
     * @return	T 缓存键值对应的数据
     */
    public <T> T get(final String k)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(k);
    }

    /**
     * @Title	expire
     * @Desc	设置有效时间
     * @Date	2024-03-23 10:29:18
     * @Param	k Redis键
     * @Param	timeout 超时时间 TimeUnit
     * @return	Boolean Boolean.TRUE 设置成功; Boolean.FALSE 设置失败
     */
    public Boolean expire(final String k, final long timeout)
    {
        return expire(k, timeout, TimeUnit.SECONDS);
    }

    /**
     * @Title	expire
     * @Desc	设置有效时间
     * @Date	2024-03-23 10:30:46
     * @Param	k Redis键
     * @Param	timeout 超时时间
     * @Param	unit 时间单位 TimeUnit
     * @return	Boolean Boolean.TRUE 设置成功; Boolean.FALSE 设置失败
     */
    public Boolean expire(final String k, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(k, timeout, unit);
    }

    /**
     * @Title	getExpire
     * @Desc	获取有效时间
     * @Date	2024-03-23 10:32:09
     * @Author	HuaAo
     * @Param	k Redis键
     * @return	Long 有效时间
     */
    public Long getExpire(final String k)
    {
        return redisTemplate.getExpire(k);
    }

    /**
     * @Title	hasKey
     * @Desc	判断 key是否存在
     * @Date	2024-03-23 10:32:26
     * @Param	k Redis键
     * @return	Boolean Boolean.TRUE 设置成功; Boolean.FALSE 设置失败
     */
    public Boolean hasKey(String k)
    {
        return redisTemplate.hasKey(k);
    }

    /**
     * @Title	delete
     * @Desc	删除单个对象
     * @Date	2024-03-23 10:34:49
     * @Author	HuaAo
     * @Param	k Redis键
     * @return	Boolean  Boolean.TRUE 删除成功; Boolean.FALSE 删除失败
     */
    public Boolean delete(final String k)
    {
        return redisTemplate.delete(k);
    }

    /**
     * @Title	delete
     * @Desc	删除集合对象
     * @Date	2024-03-23 10:35:55
     * @Author	HuaAo
     * @Param	c 对象集合
     * @return	Boolean Boolean.TRUE 删除成功; Boolean.FALSE 删除失败
     */
    public Boolean delete(final Collection<?> c)
    {
        Long l = redisTemplate.delete(c);
        return l != null && l > 0;
    }

    /**
     * @Title	setList
     * @Desc	缓存List数据
     * @Date	2024-03-23 11:01:58
     * @Param	k Redis键
     * @Param	list 缓存的List数据
     * @return	Long 缓存的List对象
     */
    public <T> Long setList(final String k, final List<T> list)
    {
        Long count = redisTemplate.opsForList().rightPushAll(k, list);
        return count == null ? 0 : count;
    }

    /**
     * @Title	getList
     * @Desc	获得缓存的list对象
     * @Date	2024-03-23 11:02:33
     * @Param	k Redis键
     * @return	List<T> 缓存键值对应的List数据
     */
    public <T> List<T> getList(final String k)
    {
        return redisTemplate.opsForList().range(k, 0, -1);
    }

    /**
     * @Title	setSets
     * @Desc	缓存Set数据
     * @Date	2024-03-23 11:03:40
     * @Param	k Redis键
     * @Param	sets 缓存的Set数据
     * @return	BoundSetOperations<String, T> 缓存的Set对象
     */
    public <T> BoundSetOperations<String, T> setSets(final String k, final Set<T> sets)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(k);
        for (T t : sets)
        {
            setOperation.add(t);
        }
        return setOperation;
    }

    /**
     * @Title	getSets
     * @Desc	获得缓存的Set对象
     * @Date	2024-03-23 11:05:13
     * @Param	k Redis键
     * @return	Set<T> 缓存键值对应的Set数据
     */
    public <T> Set<T> getSets(final String k)
    {
        return redisTemplate.opsForSet().members(k);
    }

    /**
     * @Title	setMap
     * @Desc	缓存Map数据
     * @Date	2024-03-23 11:06:03
     * @Param	k Redis键
     * @Param	map 缓存的Map对象
     */
    public <T> void setMap(final String k, final Map<String, T> map)
    {
        if (k != null) { redisTemplate.opsForHash().putAll(k, map); }
    }

    /**
     * @Title	getMap
     * @Desc	获得缓存的Map对象
     * @Date	2024-03-23 11:07:41
     * @Param	k Redis键
     * @return	Map<String, T> 缓存键值对应的Map数据
     */
    public <T> Map<String, T> getMap(final String k)
    {
        return redisTemplate.opsForHash().entries(k);
    }

    /**
     * @Title	setHash
     * @Desc	往Hash中存入数据
     * @Date	2024-03-23 11:08:46
     * @Param	k Redis键
     * @Param	hash Hash键
     * @Param	v 缓存的值
     */
    public <T> void setHash(final String k, final String hash, final T v)
    {
        redisTemplate.opsForHash().put(k, hash, v);
    }

    /**
     * @Title	getHash
     * @Desc	获取Hash中的数据
     * @Date	2024-03-23 11:11:13
     * @Param	k Redis键
     * @Param	hash Hash键
     * @return	T 缓存键值对应的Map中Hash键对象
     */
    public <T> T getHash(final String k, final String hash)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(k, hash);
    }

    /**
     * @Title	getHash
     * @Desc	获取多个Hash中的数据
     * @Date	2024-03-23 11:12:27
     * @Param	k Redis键
     * @Param	hash Hash键集合
     * @return	List<T> 缓存键值对应的Map中Hash键对象集合
     */
    public <T> List<T> getHash(final String k, final Collection<Object> hash)
    {
        return redisTemplate.opsForHash().multiGet(k, hash);
    }

    /**
     * @Title	deleteHash
     * @Desc	删除Hash中的某条数据
     * @Date	2024-03-23 11:13:13
     * @Param	k Redis键
     * @Param	hash Hash键
     * @return	boolean 是否成功
     */
    public boolean deleteHash(final String k, final String hash)
    {
        return redisTemplate.opsForHash().delete(k, hash) > 0;
    }

    /**
     * @Title	keys
     * @Desc	获得缓存的基本对象列表
     * @Date	2024-03-23 11:14:14
     * @Param	pattern 字符串前缀
     * @return	Collection<String> 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }
}