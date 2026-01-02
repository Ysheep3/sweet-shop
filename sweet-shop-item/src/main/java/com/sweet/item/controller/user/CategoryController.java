package com.sweet.item.controller.user;

import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sweet.shop.common.result.Result;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/items/user/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list(CategoryDTO categoryDTO){
        List<Category> categoryList = categoryService.getByType(categoryDTO);
        return Result.success(categoryList);
    }
}
