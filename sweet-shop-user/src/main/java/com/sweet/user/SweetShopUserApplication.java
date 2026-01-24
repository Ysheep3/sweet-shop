package com.sweet.user;

import com.sweet.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.sweet")
@EnableFeignClients(basePackages = "com.sweet.api.client", defaultConfiguration = DefaultFeignConfig.class)
public class SweetShopUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(SweetShopUserApplication.class, args);
    }
}