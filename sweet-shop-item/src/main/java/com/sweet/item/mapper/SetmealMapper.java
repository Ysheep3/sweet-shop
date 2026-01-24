package com.sweet.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.item.entity.dto.SetmealPageDTO;
import com.sweet.item.entity.pojo.Setmeal;
import com.sweet.item.entity.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    IPage<SetmealVO> pageQuery(Page<SetmealVO> page, @Param("setmeal") SetmealPageDTO setmealPageDTO);
}
