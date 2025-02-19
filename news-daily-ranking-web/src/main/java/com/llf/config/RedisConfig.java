package com.llf.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.llf.model.HotSearchDetailDTO;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// news-daily-ranking-start/src/main/java/com/llf/config/RedisConfig.java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, HotSearchDetailDTO> hotSearchRedisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, HotSearchDetailDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key使用字符串序列化
        template.setKeySerializer(new StringRedisSerializer());

        // Value使用带类型信息的JSON序列化
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));

        return template;
    }

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 启用类型信息
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        // 解决Java8时间类型序列化
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}