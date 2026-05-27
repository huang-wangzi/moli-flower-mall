package com.hengzhou.moli.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
/**
 * Redis 配置类
 * 作用：统一配置Redis的序列化方式、缓存过期时间、解决时间类型报错
 */
@Configuration
public class RedisConfig {
    /**
     * 配置Redis缓存规则（全局生效）
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        // 1. 创建Jackson的ObjectMapper，用于自定义JSON序列化规则
        ObjectMapper om = new ObjectMapper();
        // 2. 设置所有权限（所有字段、方法都可以被序列化）
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 3. 注册Java8时间模块，解决LocalDateTime序列化失败问题
        om.registerModule(new JavaTimeModule());
        // 4. 开启自动类型识别 → 存到Redis时带上类全名，反序列化时能还原成原对象
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
// 5. 创建Jackson序列化器，用于把对象转成JSON存到Redis
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(om, Object.class);
        // 6. 构建并返回Redis缓存配置
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))// 设置缓存过期时间：1小时
                .serializeKeysWith(// key 使用字符串序列化
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(// value 使用Jackson JSON序列化
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues(); // 不缓存null值，避免浪费空间
    }
}