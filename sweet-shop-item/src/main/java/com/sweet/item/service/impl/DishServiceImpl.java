package com.sweet.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.item.common.ItemStatusEnum;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.pojo.Setmeal;
import com.sweet.item.entity.vo.DishVO;
import com.sweet.item.mapper.DishMapper;
import com.sweet.item.mapper.SetmealMapper;
import com.sweet.item.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.exception.CategoryErrorException;
import com.sweet.common.exception.DishGetErrorException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public List<DishVO> getDishByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_ID_NOT_NULL);
        }
        LambdaQueryWrapper<Dish> wrapper = Wrappers.lambdaQuery(Dish.class)
                .eq(Dish::getStatus, ItemStatusEnum.ENABLED.getCode());

        List<Dish> dishes = new ArrayList<>();
        List<DishVO> dishVOList = new ArrayList<>();

        if (categoryId == 0) {
            dishes = dishMapper.selectList(wrapper);
            if (CollectionUtil.isEmpty(dishes)) {
                throw new DishGetErrorException(MessageConstant.CATEGORY_HAVE_NOT_DISH);
            }

            for (Dish dish : dishes) {
                DishVO dishVO = BeanUtil.toBean(dish, DishVO.class);
                dishVOList.add(dishVO);
            }

            return dishVOList;
        }

        wrapper.eq(Dish::getCategoryId, categoryId);

        dishes = dishMapper.selectList(wrapper);

        if (CollectionUtil.isEmpty(dishes)) {
            throw new DishGetErrorException(MessageConstant.CATEGORY_HAVE_NOT_DISH);
        }

        for (Dish dish : dishes) {
            DishVO dishVO = BeanUtil.toBean(dish, DishVO.class);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public List<Object> searchAll(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            throw new DishGetErrorException(MessageConstant.SEARCH_KEYWORD_NOT_NULL);
        }
        List<Object> itemList = new ArrayList<>();

        LambdaQueryWrapper<Dish> dishWrapper = Wrappers.lambdaQuery(Dish.class)
                .eq(Dish::getStatus, ItemStatusEnum.ENABLED.getCode())
                .like(Dish::getName, keyword);

        List<Dish> dishes = dishMapper.selectList(dishWrapper);

        if (CollectionUtil.isNotEmpty(dishes)) {
            for (Dish dish : dishes) {
                itemList.add(BeanUtil.toBean(dish, DishVO.class));
            }
        }

        LambdaQueryWrapper<Setmeal> setmealWrapper = Wrappers.lambdaQuery(Setmeal.class)
                .eq(Setmeal::getStatus, ItemStatusEnum.ENABLED.getCode())
                .like(Setmeal::getName, keyword);
        List<Setmeal> setmeals = setmealMapper.selectList(setmealWrapper);

        if (CollectionUtil.isNotEmpty(setmeals)) {
            for (Setmeal setmeal : setmeals) {
                itemList.add(BeanUtil.toBean(setmeal, DishVO.class));
            }
        }

        return itemList;
    }

    /*
    @Override
    public List<Object> getAllItems(Long categoryId) {
        List<Object> itemList = new ArrayList<>();

        if (categoryId == null) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_ID_NOT_NULL);
        }

        LambdaQueryWrapper<Dish> dishWrapper = Wrappers.lambdaQuery(Dish.class)
                .eq(Dish::getStatus, ItemStatusEnum.ENABLED.getCode());

        List<Dish> dishes = dishMapper.selectList(dishWrapper);
        if (CollectionUtil.isNotEmpty(dishes)) {
            for (Dish dish : dishes) {
                itemList.add(BeanUtil.toBean(dish, DishVO.class));
            }
        }

        LambdaQueryWrapper<Setmeal> setmealWrapper = Wrappers.lambdaQuery(Setmeal.class)
                .eq(Setmeal::getStatus, ItemStatusEnum.ENABLED.getCode());
        List<Setmeal> setmeals = setmealMapper.selectList(setmealWrapper);

        if (CollectionUtil.isNotEmpty(setmeals)) {
            for (Setmeal setmeal : setmeals) {
                itemList.add(BeanUtil.toBean(setmeal, DishVO.class));
            }
        }

        return itemList;
    }
*/

    @Override
    public DishVO getById(Long id) {
        if (id == null) {
            throw new DishGetErrorException(MessageConstant.DISH_OR_SETMEAL_ID_NOT_NULL);
        }

        Dish dish = dishMapper.selectById(id);

        if (dish != null) {
            return BeanUtil.toBean(dish, DishVO.class);
        }

        throw new DishGetErrorException(MessageConstant.GET_ERROR);
    }
}
