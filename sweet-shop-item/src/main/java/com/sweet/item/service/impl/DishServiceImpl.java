package com.sweet.item.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.api.dto.DishOverViesVO;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.CategoryErrorException;
import com.sweet.common.exception.DeleteNotAllowException;
import com.sweet.common.exception.DishGetErrorException;
import com.sweet.common.exception.StopNotAllowException;
import com.sweet.common.result.PageResult;
import com.sweet.item.common.ItemStatusEnum;
import com.sweet.item.entity.dto.DishDTO;
import com.sweet.item.entity.dto.DishPageDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.pojo.Setmeal;
import com.sweet.item.entity.pojo.SetmealDish;
import com.sweet.item.entity.vo.DishVO;
import com.sweet.item.mapper.DishMapper;
import com.sweet.item.mapper.SetmealDishMapper;
import com.sweet.item.mapper.SetmealMapper;
import com.sweet.item.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;

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

    /**
     * 管理端菜品分页查询
     *
     * @param dishPageDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageDTO dishPageDTO) {
        Page<DishVO> page = new Page<>(
                dishPageDTO.getPage(),
                dishPageDTO.getPageSize()
        );
        IPage<DishVO> result = dishMapper.pageQueryByCategoryId(page, dishPageDTO);

        return new PageResult(
                result.getTotal(),
                result.getRecords()
        );
    }


    /**
     * 管理端新增菜品
     *
     * @param dishDTO
     */
    @Transactional
    public void insert(DishDTO dishDTO) {
        Dish dish = BeanUtil.toBean(dishDTO, Dish.class);
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());

        dishMapper.insert(dish);
    }

    /**
     * 管理端批量删除菜品
     *
     * @param ids
     */
    @Transactional
    public void deleteBatchByIds(List<Long> ids) {
        for (Long id : ids) {
            SetmealDish dish = setmealDishMapper.selectOne(
                    Wrappers.lambdaQuery(SetmealDish.class)
                            .eq(SetmealDish::getDishId, id));

            if (dish != null) {
                // 关联了套餐
                throw new DeleteNotAllowException(dish.getName() + MessageConstant.DELETE_DISH_ERROR_BY_SETMEAL);
            }

            Dish dish2 = dishMapper.selectOne(
                    Wrappers.lambdaQuery(Dish.class)
                            .eq(Dish::getId, id)
            );
            if (dish2.getStatus().equals(ItemStatusEnum.ENABLED.getCode())) {
                throw new DeleteNotAllowException(dish2.getName() + MessageConstant.DELETE_DISH_ERROR_BY_START);
            }

        }

        dishMapper.deleteBatchIds(ids);
    }

    /**
     * 修改菜品信息
     *
     * @param dishDTO
     */
    @Transactional
    public void updateDishWithFlavor(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setUpdateTime(LocalDateTime.now());
        dishMapper.updateById(dish);

        // 先删掉所有口味, 再插入新的
//        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(DishFlavor::getDishId, dish.getId());
//        dishFlavorMapper.delete(wrapper);
//
//        dishFlavorMapper.insert(dishDTO.getFlavors());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = dishMapper.selectById(id);
        SetmealDish dish1 = setmealDishMapper.selectOne(
                Wrappers.lambdaQuery(SetmealDish.class)
                        .eq(SetmealDish::getDishId, id)
        );

        if (dish1 != null) {
            throw new StopNotAllowException(dish1.getName()+ MessageConstant.STOP_DISH_ERROR_BY_SETMEAL);
        }

        dish.setStatus(status);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.updateById(dish);
    }

    @Override
    public List<Dish> list(Long categoryId) {
        if (categoryId == null) {
            throw new DishGetErrorException(MessageConstant.DO_ERROR);
        }

        List<Dish> dishes = dishMapper.selectList(
                Wrappers.lambdaQuery(Dish.class)
                        .eq(Dish::getCategoryId, categoryId)
        );

        if (CollUtil.isEmpty(dishes)) {
            return List.of();
        }
        return dishes;
    }

    @Override
    public DishOverViesVO overviewDishes() {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, ItemStatusEnum.DISABLED.getCode());
        long discontinued = dishMapper.selectCount(wrapper);

        wrapper.clear();
        wrapper.eq(Dish::getStatus, ItemStatusEnum.ENABLED.getCode());
        long sold = dishMapper.selectCount(wrapper);

        DishOverViesVO vo = new DishOverViesVO();
        vo.setDiscontinued((int) discontinued);
        vo.setSold((int) sold);

        return vo;
    }

}
