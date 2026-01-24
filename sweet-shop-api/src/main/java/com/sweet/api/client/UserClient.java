package com.sweet.api.client;

import com.sweet.api.dto.AddressVO;
import com.sweet.api.dto.DishVO;
import com.sweet.api.dto.SetmealVO;
import com.sweet.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "sweet-shop-user", path = "/address")
public interface UserClient {

    @GetMapping("/{id}")
    Result<AddressVO> getById(@PathVariable Long id);
}
