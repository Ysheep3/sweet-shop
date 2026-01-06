package com.sweet.item.config;

import org.apache.catalina.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import sweet.shop.common.interception.UserInfoInterception;
import sweet.shop.common.json.JacksonObjectMapper;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterception());
    }

    /**
     * 使用SpringMVC 消息转换器
     * @param converters
     */
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 设置自己的转换器
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将自己的转换器添加到容器中
        converters.add(0,converter);
    }
}
