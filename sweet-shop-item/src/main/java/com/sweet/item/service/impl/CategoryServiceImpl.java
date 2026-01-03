package com.sweet.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.item.common.ItemStatusEnum;
import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.entity.vo.CategoryVO;
import com.sweet.item.mapper.CategoryMapper;
import com.sweet.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getByType(CategoryDTO categoryDTO) {
        LambdaQueryWrapper<Category> wrapper = Wrappers.lambdaQuery(Category.class);

        List<Category> categoryList;
        List<CategoryVO> categoryVOList = new ArrayList<>();

        if (categoryDTO.getType() != null) {
            wrapper.eq(Category::getType, categoryDTO.getType())
                    .eq(Category::getStatus, ItemStatusEnum.ENABLED.getCode());
            categoryList = categoryMapper.selectList(wrapper);
            for (Category category : categoryList) {
                CategoryVO categoryVO = BeanUtil.toBean(category, CategoryVO.class);
                categoryVOList.add(categoryVO);
            }
            return categoryVOList;
        }

        wrapper.eq(Category::getStatus, ItemStatusEnum.ENABLED.getCode());
        categoryList = categoryMapper.selectList(wrapper);
        for (Category category : categoryList) {
            CategoryVO categoryVO = BeanUtil.toBean(category, CategoryVO.class);
            categoryVOList.add(categoryVO);
        }
        return categoryVOList;
    }
}
