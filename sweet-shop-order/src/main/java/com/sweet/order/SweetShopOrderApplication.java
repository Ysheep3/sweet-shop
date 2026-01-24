package com.sweet.order;

import com.sweet.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.sweet")
@EnableTransactionManagement
@EnableScheduling
@EnableFeignClients(basePackages = "com.sweet.api.client", defaultConfiguration = DefaultFeignConfig.class)
public class SweetShopOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SweetShopOrderApplication.class, args);
    }
}