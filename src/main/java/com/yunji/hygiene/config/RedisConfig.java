package com.yunji.hygiene.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunji.hygiene.util.BeanUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * RedisConfig：Redis / Redisson / Spring Cache 配置
 *
 * 主要提供：
 * 1) RedissonClient（单机模式）：用于分布式锁、限流、队列等 Redisson 能力
 * 2) RedisTemplate<Object,Object>：自定义 key/value 序列化（key=String，value=Jackson JSON）
 * 3) Lua 限流脚本（DefaultRedisScript<Long>）
 * 4) RedisCacheManager：启用 Spring Cache，并设置默认 TTL=1天 + JSON 序列化
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String host; // Redis 地址

    @Value("${spring.redis.port}")
    private String port; // Redis 端口

    @Value("${spring.redis.password}")
    private String password; // Redis 密码（可为空）

    @Value("${spring.redis.timeout:3000}")
    private int timeout; // 连接超时（ms）

    @Value("${spring.redis.retry:3}")
    private int retry; // 重试次数

    @Value("${spring.redis.database:0}")
    private int database; // Redis DB index

    /**
     * Redisson 单机模式客户端
     * - 支持分布式锁、限流、信号量、延迟队列等
     * - address 这里是 redis://host:port（非 SSL）
     * - 如果需要 SSL 连接一般用 rediss://
     */
    @Bean
    public RedissonClient singletonModeRedisson() {
        Config config = new Config();
        SingleServerConfig server = config.useSingleServer();
        server.setAddress("redis://" + host + ":" + port);
        server.setConnectTimeout(timeout);
        server.setRetryAttempts(retry);
        server.setDatabase(database);
        if (BeanUtils.isNotNull(password)) {
            server.setPassword(password);
        }
        return Redisson.create(config);
    }

    /**
     * RedisTemplate：自定义序列化
     * - key/hashKey：StringRedisSerializer（可读性好）
     * - value/hashValue：Jackson JSON（支持对象序列化）
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<Object> jackson = buildJackson();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 构建 Jackson 序列化器
     * - activateDefaultTyping：启用多态类型信息写入（反序列化需要）
     * - FAIL_ON_UNKNOWN_PROPERTIES=false：兼容字段变更
     */
    private Jackson2JsonRedisSerializer<Object> buildJackson() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();

        // 将类型信息写入 JSON（用于 Object 反序列化）
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 遇到多余字段不报错（兼容升级）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    /**
     * Lua 限流脚本 Bean（配合 RedisTemplate.execute 使用）
     * 返回值：当前计数（Long）
     */
    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /** Lua 脚本：固定窗口计数限流（incr + expire） */
    private String limitScriptText() {
        return "local key = KEYS[1]\n" +
                "local count = tonumber(ARGV[1])\n" +
                "local time = tonumber(ARGV[2])\n" +
                "local current = redis.call('get', key);\n" +
                "if current and tonumber(current) > count then\n" +
                "    return tonumber(current);\n" +
                "end\n" +
                "current = redis.call('incr', key)\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call('expire', key, time)\n" +
                "end\n" +
                "return tonumber(current);";
    }

    /**
     * Spring Cache 缓存管理器
     * - 默认 TTL：1天
     * - key：String 序列化
     * - value：Jackson JSON 序列化
     * - 禁止缓存 null 值（避免穿透时写入大量空值）
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(buildJackson()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(configuration)
                .build();
    }
}
