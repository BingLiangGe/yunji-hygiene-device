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

@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    @Value("${spring.redis.host}")
    private String host; // 连接地址

    @Value("${spring.redis.port}")
    private String port; // 连接端口

    @Value("${spring.redis.password}")
    private String password; // 连接密码

    @Value("${spring.redis.timeout:3000}")
    private int timeout; // 连接超时时间

    @Value("${spring.redis.retry:3}")
    private int retry;    // 重试次数

    @Value("${spring.redis.database:0}")
    private int database;


    @Bean
    public RedissonClient singletonModeRedisson() {
        Config config = new Config();
        // 使⽤"redis://"来启⽤SSL连接
        SingleServerConfig server = config.useSingleServer();
        server.setAddress("redis://" + host + ":" + port);
        server.setConnectTimeout(timeout);
        // 重试次数
        server.setRetryAttempts(retry);
        // 设置db 不设置使用第0个
        server.setDatabase(database);
        if (BeanUtils.isNotNull(password)) {
            server.setPassword(password);
        }
        return Redisson.create(config);
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        Jackson2JsonRedisSerializer<Object> jackson = buildJackson();
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson);
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson);
        template.afterPropertiesSet();
        return template;
    }

    private Jackson2JsonRedisSerializer<Object>  buildJackson() {
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        serializer.setObjectMapper(objectMapper);
        return serializer;
    }

    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private String limitScriptText() {
        return "local key = KEYS[1]\n" + "local count = tonumber(ARGV[1])\n" + "local time = tonumber(ARGV[2])\n" + "local current = redis.call('get', key);\n" + "if current and tonumber(current) > count then\n" + "    return tonumber(current);\n" + "end\n" + "current = redis.call('incr', key)\n" + "if tonumber(current) == 1 then\n" + "    redis.call('expire', key, time)\n" + "end\n" + "return tonumber(current);";
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 序列化key
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)) //配置缓存数据的默认存活时间1天
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer)) // 指定key进行序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(buildJackson())).disableCachingNullValues(); // null不参与序列化操作
        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(configuration).build();
    }

}
