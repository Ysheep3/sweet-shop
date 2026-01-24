package com.sweet.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sweet.alipay")
public class AlipayProperties {

    /** 小程序 APPID */
    private String appId;

    /** 应用私钥 */
    private String privateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 网关地址（沙盒 / 正式） */
    private String gatewayUrl;

    /** 异步回调 */
    private String notifyUrl;

    /** 编码 */
    private String charset = "UTF-8";

    /** 签名方式 */
    private String signType = "RSA2";
}
