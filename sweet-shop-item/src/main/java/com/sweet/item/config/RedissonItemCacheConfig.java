package com.sweet.item.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * item 服务专用：为“分类/菜品/套餐”查询缓存提供 JSON 编解码。
 *
 * <p>注意：不修改全局 RedissonClient 的 codec（避免影响其它模块/业务）。</p>
 */
@Configuration
public class RedissonItemCacheConfig {

    @Bean
    public ObjectMapper redissonItemObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public JsonJacksonCodec redissonItemJsonCodec(ObjectMapper redissonItemObjectMapper) {
        return new JsonJacksonCodec(redissonItemObjectMapper);
    }
}


