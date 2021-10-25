package com.natsu.template.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import javax.annotation.Resource;
import java.time.Duration;

/**
 * Created by sunyu on 2021-10-25
 */
@Configuration
public class RedisConfig {

    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;

    @Bean
    public GenericToStringSerializer<String> genericToStringSerializer() {
        return new GenericToStringSerializer<>(String.class);
    }

    @Bean("stringRedisTemplate")
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setDefaultSerializer(genericToStringSerializer());
        stringRedisTemplate.setKeySerializer(genericToStringSerializer());
        stringRedisTemplate.setValueSerializer(genericToStringSerializer());
        stringRedisTemplate.setHashKeySerializer(genericToStringSerializer());
        stringRedisTemplate.setHashValueSerializer(genericToStringSerializer());
        stringRedisTemplate.setConnectionFactory(lettuceConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    @Bean
    public CacheManager cacheManager() {
        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
                .transactionAware()
                .build();
    }
}
