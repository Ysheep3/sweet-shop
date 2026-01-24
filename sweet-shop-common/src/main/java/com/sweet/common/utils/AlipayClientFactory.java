package com.sweet.common.utils;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.sweet.common.properties.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
@ConditionalOnProperty(prefix = "sweet.alipay", name = "app-id")
public class AlipayClientFactory {

    @Bean
    public AlipayClient alipayClient(AlipayProperties props) {
        AlipayClient alipayClient = new DefaultAlipayClient(
                props.getGatewayUrl(),
                props.getAppId(),
                props.getPrivateKey(),
                "json",
                props.getCharset(),
                props.getAlipayPublicKey(),
                props.getSignType()
        );
        System.out.println("=======支付宝SDK初始化成功=======");

        return alipayClient;
    }
}