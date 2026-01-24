package com.sweet.item.controller.admin;


import cn.hutool.db.Db;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sweet.api.dto.DishOverViesVO;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.StopNotAllowException;
import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.item.entity.dto.DishDTO;
import com.sweet.item.entity.dto.DishPageDTO;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.pojo.SetmealDish;
import com.sweet.item.entity.vo.DishVO;
import com.sweet.item.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    //@CacheEvict(cacheNames = "dishCache", allEntries = true)
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
    //@CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<Void> startOrStop(@PathVariable Integer status, Long id){
        dishService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    //@CacheEvict(cacheNames = "dishCache", allEntries = true)
    public Result<Void> update(@RequestBody DishDTO dishDTO) {
        dishService.updateDishWithFlavor(dishDTO);
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
