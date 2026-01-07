package com.sweet.api.client;

import com.sweet.api.dto.DishVO;
import com.sweet.api.dto.SetmealVO;
import com.sweet.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "sweet-shop-item", path = "/items/user")
public interface ItemClient {

    @GetMapping("/dish/{id}")
    Result<DishVO> getDishById(@PathVariable Long id);

    @GetMapping("/setmeal/{id}")
    Result<SetmealVO> getSetmealById(@PathVariable Long id);
}
