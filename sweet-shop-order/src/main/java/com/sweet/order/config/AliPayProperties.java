//package com.sweet.order.config;
//
//import com.alipay.api.AlipayClient;
//import com.alipay.api.DefaultAlipayClient;
//import lombok.Data;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Data
//@Configuration
//@ConfigurationProperties(prefix = "sweet.alipay")
//public class AliPayProperties {
//
//    private String appId;
//    private String privateKey;
//    private String alipayPublicKey;
//    private String notifyUrl;
//
//    /**
//     * 沙盒网关
//     */
//    private String gatewayUrl;
//
//    /**
//     * 编码
//     */
//    private String charset;
//
//    /**
//     * 签名方式
//     */
//    private String signType;
//
//    @Bean
//    public AlipayClient alipayClient() {
//        return new DefaultAlipayClient(
//                gatewayUrl,
//                appId,
//                privateKey,
//                "json",
//                charset,
//                alipayPublicKey,
//                signType
//        );
//    }
//}
