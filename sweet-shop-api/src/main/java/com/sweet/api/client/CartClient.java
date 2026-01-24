package com.sweet.api.client;

import com.sweet.api.dto.ShoppingCartVO;
import com.sweet.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "sweet-shop-cart", path = "/shopping-cart")
public interface CartClient {

    @GetMapping("/listByIds")
    Result<List<ShoppingCartVO>> listByIds(@RequestParam("ids") List<Long> ids);

    @DeleteMapping("/deleteByIds")
    void deleteByIds(@RequestParam("ids") List<Long> ids);

    @PostMapping("/again")
    void again(@RequestBody List<ShoppingCartVO> shoppingCartVOS);
}
