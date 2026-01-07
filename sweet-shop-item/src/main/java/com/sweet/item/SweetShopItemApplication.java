package com.sweet.item;

import com.sweet.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.sweet.api.client", defaultConfiguration = DefaultFeignConfig.class)
public class SweetShopItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SweetShopItemApplication.class, args);
    }
}