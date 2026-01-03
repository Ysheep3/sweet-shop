package com.sweet.item.service;

import com.sweet.item.entity.vo.DishVO;

import java.util.List;

public interface DishService {
    List<DishVO> getDishByCategoryId(Long categoryId);

    List<Object> searchAll(String keyword);

    List<Object> getAllItems(Long categoryId);
}
