package com.sweet.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.item.common.ItemStatusEnum;
import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.mapper.CategoryMapper;
import com.sweet.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> getByType(CategoryDTO categoryDTO) {
        LambdaQueryWrapper<Category> wrapper = Wrappers.lambdaQuery(Category.class);

        List<Category> categoryList;
        if (categoryDTO.getType() != null) {
            wrapper.eq(Category::getType, categoryDTO.getType())
                    .eq(Category::getStatus, ItemStatusEnum.ENABLED.getCode());
            categoryList = categoryMapper.selectList(wrapper);
            return categoryList;
        }

        wrapper.eq(Category::getStatus, ItemStatusEnum.ENABLED.getCode());

        categoryList = categoryMapper.selectList(wrapper);
        return categoryList;
    }
}
