package com.sweet.gateway.filters;

import com.sweet.gateway.properties.AuthProperties;
import com.sweet.gateway.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.sweet.common.utils.JwtUtil;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final JwtProperties jwtProperties;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取request
        ServerHttpRequest request = exchange.getRequest();

        // 2. 判断请求的路径是否需要登录验证
        if (isExclude(request.getPath().toString())) {
            return chain.filter(exchange);
        }

        // 3. 获取token
        HttpHeaders httpHeaders = request.getHeaders();

        List<String> adminHeaders = httpHeaders.get(jwtProperties.getAdminTokenName());
        String adminToken = null;
        if (adminHeaders != null && !adminHeaders.isEmpty()) {
            adminToken = adminHeaders.get(0);
        }

        List<String> userHeaders = httpHeaders.get(jwtProperties.getUserTokenName());
        String userToken = null;
        if (userHeaders != null && !userHeaders.isEmpty()) {
            userToken = userHeaders.get(0);
        }

        List<String> riderHeaders = httpHeaders.get(jwtProperties.getRiderTokenName());
        String riderToken = null;
        if (riderHeaders != null && !riderHeaders.isEmpty()) {
            riderToken = riderHeaders.get(0);
        }

        // 5. 校验并解析密钥
        Long userId = null;
        if (userToken != null) {
            try {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), userToken);
                userId = Long.valueOf(claims.get("userId").toString());
            } catch (RuntimeException e) {
                return unauthorized(exchange);
            }
        }

        if (adminToken != null) {
            try {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), adminToken);
                userId = Long.valueOf(claims.get("adminId").toString());
            } catch (RuntimeException e) {
                return unauthorized(exchange);
            }
        }

        if (riderToken != null) {
            try {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getRiderSecretKey(), riderToken);
                userId = Long.valueOf(claims.get("riderId").toString());
            } catch (RuntimeException e) {
                return unauthorized(exchange);
            }
        }

        if (userId == null) {
            return unauthorized(exchange);
        }

        // 6. 传递用户id到 别的微服务
        System.out.println("userId = " + userId);
        String userInfo = userId.toString();

        ServerWebExchange webExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();

        // 放行
        return chain.filter(webExchange);
    }

    @NonNullDecl
    private static Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        return response.setComplete();
    }

    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(excludePath, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
