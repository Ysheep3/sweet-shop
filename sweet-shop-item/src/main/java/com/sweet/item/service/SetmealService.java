package com.sweet.item.service;

import com.sweet.item.entity.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetmealService {
    List<SetmealVO> getSetmealByCategoryId(Long categoryId);

    SetmealVO getById(Long id);
}
