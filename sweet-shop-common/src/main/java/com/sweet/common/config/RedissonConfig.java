package com.sweet.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class RedissonConfig {
//
//    @Bean(destroyMethod = "shutdown")
//    public RedissonClient redissonClient() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        Config config = new Config();
//        config.setCodec(new JsonJacksonCodec(mapper));
//        config.useSingleServer()
//                .setAddress("redis://172.31.41.102:6379");
//        return Redisson.create(config);
//    }
//}
