package com.sweet.api.client;

import com.sweet.api.dto.*;
import com.sweet.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "sweet-shop-item", path = "/items")
public interface ItemClient {

    @GetMapping("/user/dish/{id}")
    Result<DishVO> getDishById(@PathVariable Long id);

    @GetMapping("/user/setmeal/{id}")
    Result<SetmealVO> getSetmealById(@PathVariable Long id);

    @GetMapping("/admin/setmeal/overviewSetmeals")
    Result<SetmealOverViewVO> overviewSetmeals();

    @GetMapping("/admin/dish/overviewDishes")
    Result<DishOverViesVO> overviewDishes();
}
