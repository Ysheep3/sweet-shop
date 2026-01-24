package com.sweet.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.item.entity.dto.DishPageDTO;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.entity.pojo.Dish;
import com.sweet.item.entity.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {


    IPage<DishVO> pageQueryByCategoryId(Page<DishVO> page, @Param("dish") DishPageDTO dishPageDTO);
}
