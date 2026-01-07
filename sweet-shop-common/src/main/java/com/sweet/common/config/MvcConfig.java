package com.sweet.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.sweet.common.interception.UserInfoInterception;
import com.sweet.common.json.JacksonObjectMapper;

import java.util.List;

/**
 * 网关的项目引入了common模块,gateway不是基于mvc,所以会报错
 * 网关不使用Mvc, 使用Mvc的都有DispatcherServlet类
 * 为了不在网关中使用, 所以配置 ConditionalOnClass有条件的自动装配, 若没有DispatcherServlet该类 不加载这个配置类
 */

@Configuration
@ConditionalOnClass(DispatcherServlet.class)
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterception());
    }

    /**
     * 使用SpringMVC 消息转换器
     * @param converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 设置自己的转换器
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将自己的转换器添加到容器中
        converters.add(0,converter);
    }
}
