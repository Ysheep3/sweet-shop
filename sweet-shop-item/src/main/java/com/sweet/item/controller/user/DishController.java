package com.sweet.item.controller.user;

import com.sweet.item.entity.vo.DishVO;
import com.sweet.item.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sweet.shop.common.result.Result;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/items/user/dish")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {
        List<DishVO> dishVOList = dishService.getDishByCategoryId(categoryId);
        return Result.success(dishVOList);
    }

    @GetMapping("/search")
    public Result<List<Object>> search(String keyword) {
        List<Object> Items = dishService.searchAll(keyword);
        return Result.success(Items);
    }

    @GetMapping("/all")
    public Result<List<Object>> getAllItems(Long categoryId) {
        List<Object> items = dishService.getAllItems(categoryId);
        return Result.success(items);
    }
}
