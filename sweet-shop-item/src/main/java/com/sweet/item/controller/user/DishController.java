package com.sweet.item.controller.user;

import cn.hutool.core.collection.CollUtil;
import com.sweet.item.entity.vo.DishVO;
import com.sweet.item.service.DishService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sweet.common.result.Result;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/items/user/dish")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;
    private final RedissonClient redissonClient;
    private final JsonJacksonCodec redissonItemJsonCodec;
    private final static String KEY = "dish::";

    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {
        RBucket<List<DishVO>> bucket = redissonClient.getBucket(KEY + categoryId, redissonItemJsonCodec);
        List<DishVO> dishVOList = bucket.get();
        if (CollUtil.isEmpty(dishVOList)) {
            dishVOList = dishService.getDishByCategoryId(categoryId);
            bucket.set(dishVOList);
        }

        return Result.success(dishVOList);
    }

    @GetMapping("/search")
    public Result<List<Object>> search(String keyword) {
        List<Object> Items = dishService.searchAll(keyword);
        return Result.success(Items);
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getById(id);
        return Result.success(dishVO);
    }
}
