package com.sweet.item.service;

import com.sweet.api.dto.SetmealOverViewVO;
import com.sweet.common.result.PageResult;
import com.sweet.item.entity.dto.SetmealDTO;
import com.sweet.item.entity.dto.SetmealPageDTO;
import com.sweet.item.entity.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetmealService {
    List<SetmealVO> getSetmealByCategoryId(Long categoryId);

    SetmealVO getById(Long id);

    /**
     * 管理端分页查询
     *
     * @param setmealPageDTO
     * @return
     */
    PageResult pageQuery(SetmealPageDTO setmealPageDTO);

    /**
     * 添加套餐
     *
     * @param setmealDTO
     */
    void insert(SetmealDTO setmealDTO);

    /**
     * 删除套餐
     *
     * @param ids
     */
    void deleteBatchByIds(List<Long> ids);

    /**
     * 修改套餐信息
     *
     * @param setmealDTO
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 起售与停售
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 查询套餐总览
     *
     * @return
     */
    SetmealOverViewVO overviewSetmeals();
}
