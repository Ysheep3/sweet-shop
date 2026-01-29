package com.sweet.common.config;

import com.sweet.common.properties.AliOssProperties;
import com.sweet.common.properties.AlipayProperties;
import com.sweet.common.utils.AliOssUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliOssProperties.class)
@ConditionalOnProperty(prefix = "sweet.oss", name = "endpoint")
public class AliOssConfig {
    @Bean
    public AliOssUtil getAliOssUtil(AliOssProperties properties) {
        return new AliOssUtil(properties.getEndpoint(),
                        properties.getAccessKeyId(),
                        properties.getAccessKeySecret(),
                        properties.getBucketName());
    }
}
