package com.sweet.user.utils;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.sweet.user.common.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlipayClientFactory {

    @Autowired
    private AlipayProperties alipayProperties;

    public AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),      // 沙箱网关
                alipayProperties.getAppId(),           // APPID
                alipayProperties.getPrivateKey(),      // 应用私钥
                "json",                            // 格式
                "UTF-8",                           // 编码
                alipayProperties.getAlipayPublicKey(), // 支付宝公钥
                "RSA2"                             // 签名方式 RSA2
        );
    }
}