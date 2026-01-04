package com.sweet.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.item.common.ItemStatusEnum;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.pojo.Setmeal;
import com.sweet.item.entity.pojo.SetmealDish;
import com.sweet.item.entity.vo.DishVO;
import com.sweet.item.entity.vo.SetmealVO;
import com.sweet.item.mapper.SetmealDishMapper;
import com.sweet.item.mapper.SetmealMapper;
import com.sweet.item.service.SetmealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sweet.shop.common.constant.MessageConstant;
import sweet.shop.common.exception.CategoryErrorException;
import sweet.shop.common.exception.DishGetErrorException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;

    @Override
    public List<SetmealVO> getSetmealByCategoryId(Long categoryId) {
        if (categoryId == null) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_ID_NOT_NULL);
        }

        LambdaQueryWrapper<Setmeal> wrapper = Wrappers.lambdaQuery(Setmeal.class)
                .eq(Setmeal::getStatus, ItemStatusEnum.ENABLED.getCode());
        List<Setmeal> setmealList = new ArrayList<>();
        List<SetmealVO> setmealVOList = new ArrayList<>();

        if (categoryId == 0) {
            setmealList = setmealMapper.selectList(wrapper);

            for (Setmeal setmeal : setmealList) {
                SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
                setmealVOList.add(setmealVO);
            }
            return setmealVOList;
        }

        wrapper.eq(Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, ItemStatusEnum.ENABLED.getCode());
        setmealList = setmealMapper.selectList(wrapper);

        if (CollectionUtil.isEmpty(setmealList)) {
            throw new CategoryErrorException(MessageConstant.CATEGORY_HAVE_NOT_SETMEAL);
        }

        for (Setmeal setmeal : setmealList) {
            SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
            setmealVOList.add(setmealVO);
        }
        return setmealVOList;
    }

    @Override
    public SetmealVO getById(Long id) {
        if (id == null) {
            throw new DishGetErrorException(MessageConstant.DISH_OR_SETMEAL_ID_NOT_NULL);
        }

        Setmeal setmeal = setmealMapper.selectById(id);

        if (setmeal != null) {
            SetmealVO setmealVO = BeanUtil.toBean(setmeal, SetmealVO.class);
            LambdaQueryWrapper<SetmealDish> wrapper = Wrappers.lambdaQuery(SetmealDish.class)
                    .eq(SetmealDish::getSetmealId, id);

            List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);
            if (CollectionUtil.isNotEmpty(setmealDishes)) {
                setmealVO.setSetmealDishes(setmealDishes);
                return setmealVO;
            }
        }

        throw new DishGetErrorException(MessageConstant.GET_ERROR);
    }
}
