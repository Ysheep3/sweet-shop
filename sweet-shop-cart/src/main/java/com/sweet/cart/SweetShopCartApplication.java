package com.sweet.cart;

import com.sweet.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableFeignClients(basePackages = "com.sweet.api.client", defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class SweetShopCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(SweetShopCartApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}