package com.sweet.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * 动态路由配置
 *
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoader {
    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final String dataId = "gateway.json";
    private final String group = "DEFAULT_GROUP";
    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct   // 在启动项目后先加载这个bean的方法
    public void routerUpdate() throws NacosException {
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // (第一次之后)监听到配置变更，更新路由表
                        update(configInfo);
                    }
                });

        // 第一次读取配置之后，更新路由表
        update(configInfo);
    }


    public void update(String configInfo) {
        log.info("监听到的路由配置信息：{}", configInfo);
        List<RouteDefinition> definitions = JSONUtil.toList(configInfo, RouteDefinition.class);

        // 旧的配置都删除
        for (String routeId : routeIds) {
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();

        // 保存配置， 若变更配置信息是 删除了某些配置，这里的保存不会其效果，所以在变更信息之前把旧的配置都删除
        for (RouteDefinition definition : definitions) {
            routeDefinitionWriter.save(Mono.just(definition)).subscribe();

            // 保存routeId， 方便后续删除
            routeIds.add(definition.getId());
        }
    }
}
