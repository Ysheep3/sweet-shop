package com.sweet.item.controller.user;

import cn.hutool.core.collection.CollUtil;
import com.sweet.common.result.Result;
import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.vo.CategoryVO;
import com.sweet.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/items/user/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final RedissonClient redissonClient;
    private final JsonJacksonCodec redissonItemJsonCodec;

    @GetMapping("/list")
    public Result<List<CategoryVO>> list(CategoryDTO categoryDTO) {
        String KEY = "category";

        RBucket<List<CategoryVO>> bucket = redissonClient.getBucket(KEY, redissonItemJsonCodec);
        List<CategoryVO> categoryVOList = bucket.get();
        if (CollUtil.isEmpty(categoryVOList)) {
            categoryVOList = categoryService.getByType(categoryDTO);
            bucket.set(categoryVOList);
        }
        return Result.success(categoryVOList);
    }
}
