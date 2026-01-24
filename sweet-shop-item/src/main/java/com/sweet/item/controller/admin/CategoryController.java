package com.sweet.item.controller.admin;

import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.dto.CategoryPageQueryDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜品及套餐分类 前端控制器
 * </p>
 *
 * @author Ysheep
 * @since 2024-11-19
 */
@RestController("adminCategoryController")
@RequestMapping("/items/admin/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    //private RedisTemplate<String, Object> redisTemplate;
    private static final String KEY = "categoryCache";
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageDTO) {
        PageResult pageResult = categoryService.pageQuery(categoryPageDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CategoryDTO categoryDTO) {
        //redisTemplate.delete(KEY);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<Void> startOrStop(@PathVariable Integer status, Long id) {
        //redisTemplate.delete(KEY);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CategoryDTO categoryDTO) {
        //redisTemplate.delete(KEY);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete(Long id) {
        //redisTemplate.delete(KEY);
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Category>> list(Integer type) {
        List<Category> categoryList = categoryService.getCategoryListByType(type);
        return Result.success(categoryList);
    }
}
