package com.sweet.user.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.exception.BaseException;
import com.sweet.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    private final RedissonClient redissonClient;
   // private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY = "shop:status";

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        RBucket<Integer> bucket = redissonClient.getBucket(KEY);
        Integer status = bucket.get();

        if (status == null) {
            throw new BaseException(MessageConstant.SHOP_STATUS_NULL);
        }
        log.info("店铺营业状态:{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

    @PutMapping("/{status}")
    public Result<Void> setStatus(@PathVariable Integer status) {
        RBucket<Integer> bucket = redissonClient.getBucket(KEY);
        bucket.set(status);
        return Result.success();
    }
}
