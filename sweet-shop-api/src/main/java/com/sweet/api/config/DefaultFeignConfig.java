package com.sweet.api.config;

import com.sweet.common.context.BaseContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * OpenFeign拦截器
     * 微服务与微服务之间传递用户信息
     * @return
     */
    @Bean
    public RequestInterceptor userInfoRequestInterception() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long userId = BaseContext.getCurrentId();
                if (userId != null) {
                    requestTemplate.header("user-info", userId.toString());
                }
            }
        };
    }

    // 配置ItemClient的降级工厂
//    @Bean
//    public ItemClientFallBackFactory itemClientFallBackFactory() {
//        return new ItemClientFallBackFactory();
//    }
}
