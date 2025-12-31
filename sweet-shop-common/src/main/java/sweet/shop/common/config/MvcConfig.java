package sweet.shop.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sweet.shop.common.interception.UserInfoInterception;

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

}
