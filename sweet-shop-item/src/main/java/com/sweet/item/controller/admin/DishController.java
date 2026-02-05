package com.sweet.item.controller.admin;


import com.sweet.api.dto.DishOverViesVO;
import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.item.entity.dto.DishDTO;
import com.sweet.item.entity.dto.DishPageDTO;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.vo.DishVO;
import com.sweet.item.service.DishService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜品 前端控制器
 * </p>
 *
 * @author Ysheep
 * @since 2024-11-19
 */
@RestController("adminDishController")
@RequestMapping("/items/admin/dish")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;
    private final RedissonClient redissonClient;
    private final static String KEY = "dish::";

    @GetMapping("/page")
    public Result<PageResult> page(DishPageDTO dishPageDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> save(@RequestBody DishDTO dishDTO) {
        dishService.insert(dishDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete(@RequestParam List<Long> ids) {
        dishService.deleteBatchByIds(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }

    @PostMapping("/status/{status}")
    public Result<Void> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody DishDTO dishDTO) {
        redissonClient.getBucket(KEY + dishDTO.getCategoryId()).delete();
        redissonClient.getBucket(KEY + 0).delete();
        dishService.updateDish(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> dishes = dishService.list(categoryId);
        return Result.success(dishes);
    }

    @GetMapping("/overviewDishes")
    Result<DishOverViesVO> overviewDishes() {
        DishOverViesVO vo = dishService.overviewDishes();
        return Result.success(vo);
    }
}
