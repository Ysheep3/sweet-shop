package com.sweet.api.client;

import com.sweet.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "sweet-shop-coupon", path = "/user-coupon/user", contextId = "userCouponClient")
public interface UserCouponClient {

    @PostMapping("/use/{id}")
    Result<Void> useCoupon(@PathVariable Long id);
}
