package com.sweet.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.Db;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.DeleteNotAllowException;
import com.sweet.common.exception.StopNotAllowException;
import com.sweet.common.result.PageResult;
import com.sweet.item.common.ItemStatusEnum;
import com.sweet.item.entity.dto.CategoryDTO;
import com.sweet.item.entity.dto.CategoryPageQueryDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.pojo.Setmeal;
import com.sweet.item.entity.vo.CategoryVO;
import com.sweet.item.mapper.CategoryMapper;
import com.sweet.item.mapper.DishMapper;
import com.sweet.item.mapper.SetmealMapper;
import com.sweet.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

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

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageDTO) {
        IPage<Category> page = new Page<>(categoryPageDTO.getPage(), categoryPageDTO.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(categoryPageDTO.getType() != null,Category::getType, categoryPageDTO.getType());

        wrapper.like(categoryPageDTO.getName() != null, Category::getName, categoryPageDTO.getName());


        categoryMapper.selectPage(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        category.setCreateUser(BaseContext.getCurrentId());

        category.setUpdateUser(BaseContext.getCurrentId());
        category.setStatus(ItemStatusEnum.ENABLED.getCode());

        categoryMapper.insert(category);
    }

    /**
     * 启用和禁用分类
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //Long dish = Db.lambdaQuery(Dish.class).eq(Dish::getCategoryId, id).count();
        //Long setmeal = Db.lambdaQuery(Setmeal.class).eq(Setmeal::getCategoryId, id).count();

        Long dish = dishMapper.selectCount(
                Wrappers.lambdaQuery(Dish.class)
                        .eq(Dish::getCategoryId, id)
        );

        Long setmeal = setmealMapper.selectCount(
                Wrappers.lambdaQuery(Setmeal.class)
                        .eq(Setmeal::getCategoryId, id)
        );

        if (dish > 0) {
            throw new StopNotAllowException(MessageConstant.STOP_CATEGORY_ERROR_BY_DISH);
        }
        if (setmeal > 0 ) {
            throw new StopNotAllowException(MessageConstant.STOP_CATEGORY_ERROR_BY_SETMEAL);
        }

        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();

        categoryMapper.updateById(category);
    }

    /**
     * 修改分类信息
     *
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        categoryMapper.updateById(category);
    }

    /**
     * 根据id删除分类
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
//        Long dish = Db.lambdaQuery(Dish.class).eq(Dish::getCategoryId, id).count();
//        Long setmeal = Db.lambdaQuery(Setmeal.class).eq(Setmeal::getCategoryId, id).count();

        Long dish = dishMapper.selectCount(
                Wrappers.lambdaQuery(Dish.class)
                        .eq(Dish::getCategoryId, id)
        );

        Long setmeal = setmealMapper.selectCount(
                Wrappers.lambdaQuery(Setmeal.class)
                        .eq(Setmeal::getCategoryId, id)
        );
        Category category = categoryMapper.selectById(id);

        if (category.getStatus().equals(ItemStatusEnum.ENABLED.getCode())) {
            throw new DeleteNotAllowException(MessageConstant.DELETE_CATEGORY_ERROR_BY_START);
        }
        if (dish > 0) {
            throw new DeleteNotAllowException(MessageConstant.DELETE_CATEGORY_ERROR_BY_DISH);
        }
        if (setmeal > 0) {
            throw new DeleteNotAllowException(MessageConstant.DELETE_CATEGORY_ERROR_BY_SETMEAL);
        }

        categoryMapper.deleteById(id);
    }

    /**
     * 查询分列表
     *
     * @param categoryDTO
     * @return
     */
    @Override
    public List<Category> list(CategoryDTO categoryDTO) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        List<Category> categoryList = new ArrayList<>();

        if (categoryDTO.getType() != null) {
            wrapper.eq(Category::getType,categoryDTO.getType());
            categoryList = categoryMapper.selectList(wrapper);
            return categoryList;
        }

        categoryList = categoryMapper.selectList(wrapper);
        return categoryList;
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    public List<Category> getCategoryListByType(Integer type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getType, type);
        return categoryMapper.selectList(wrapper);
    }
}
