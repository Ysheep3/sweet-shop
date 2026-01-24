package com.sweet.item.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sweet.api.dto.DishOverViesVO;
import com.sweet.common.result.PageResult;
import com.sweet.item.entity.dto.DishDTO;
import com.sweet.item.entity.dto.DishPageDTO;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.vo.DishVO;

import java.util.List;

public interface DishService {
    List<DishVO> getDishByCategoryId(Long categoryId);

    List<Object> searchAll(String keyword);

    //List<Object> getAllItems(Long categoryId);

    /**
     * 根据id查询菜品信息
     *
     * @param id
     * @return
     */
    DishVO getById(Long id);

    /**
     * 管理端菜品分页查询
     *
     * @param dishPageDTO
     * @return
     */
    PageResult pageQuery(DishPageDTO dishPageDTO);

    /**
     * 管理端新增菜品
     *
     * @param dishDTO
     */
    void insert(DishDTO dishDTO);

    /**
     * 管理端批量删除菜品
     *
     * @param ids
     */
    void deleteBatchByIds(List<Long> ids);

    /**
     * 修改菜品信息
     *
     * @param dishDTO
     */
    void updateDishWithFlavor(DishDTO dishDTO);

    /**
     * 起售停售
     *
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    List<Dish> list(Long categoryId);

    /**
     * 菜品总览
     * @return
     */
    DishOverViesVO overviewDishes();
}
