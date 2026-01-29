package com.sweet.api.client;

import com.sweet.api.dto.CouponVO;
import com.sweet.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "sweet-shop-coupon", path = "/coupon", contextId = "couponClient")
public interface CouponClient {

    @GetMapping("/user/{id}")
    Result<CouponVO> getById(@PathVariable Long id);

}
