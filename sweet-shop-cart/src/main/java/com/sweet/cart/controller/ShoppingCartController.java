package com.sweet.cart.controller;

import com.sweet.cart.entity.dto.ShoppingCartDTO;
import com.sweet.cart.entity.vo.ShoppingCartVO;
import com.sweet.cart.service.ShoppingCartService;
import com.sweet.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("shoppingCartController")
@RequiredArgsConstructor
@RequestMapping("/shopping-cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<Void> addItemToCart(@RequestBody ShoppingCartDTO requestParam) {
        shoppingCartService.addItemToCart(requestParam);
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Integer> getCartCount() {
        Integer count = shoppingCartService.getCount();
        return Result.success(count);
    }

    @GetMapping("/list")
    public Result<List<ShoppingCartVO>> list() {
        List<ShoppingCartVO> shoppingCartVOS = shoppingCartService.list();
        return Result.success(shoppingCartVOS);
    }

    @PostMapping("/delete")
    public Result<Void> sub(@RequestBody ShoppingCartDTO requestParam) {
        shoppingCartService.delete(requestParam);
        return Result.success();
    }
}
