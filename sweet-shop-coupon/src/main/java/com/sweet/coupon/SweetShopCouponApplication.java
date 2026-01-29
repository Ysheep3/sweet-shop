package com.sweet.coupon;

import com.sweet.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.sweet.api.client", defaultConfiguration = DefaultFeignConfig.class)
public class SweetShopCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(SweetShopCouponApplication.class, args);
    }
}