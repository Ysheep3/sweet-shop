package com.sweet.item.controller.user;

import cn.hutool.core.collection.CollUtil;
import com.sweet.item.entity.vo.SetmealVO;
import com.sweet.item.service.SetmealService;
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

@RestController("userSetmealController")
@RequestMapping("/items/user/setmeal")
@RequiredArgsConstructor
public class SetmealController {
    private final SetmealService setmealService;
    private final RedissonClient redissonClient;
    private final JsonJacksonCodec redissonItemJsonCodec;
    private final static String KEY = "setmeal::";

    @GetMapping("/list")
    public Result<List<SetmealVO>> list(Long categoryId) {
        RBucket<List<SetmealVO>> bucket = redissonClient.getBucket(KEY + categoryId, redissonItemJsonCodec);
        List<SetmealVO> setmealVOList = bucket.get();
        if (CollUtil.isEmpty(setmealVOList)) {
            setmealVOList = setmealService.getSetmealByCategoryId(categoryId);
            bucket.set(setmealVOList);
        }
        return Result.success(setmealVOList);
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }
}
