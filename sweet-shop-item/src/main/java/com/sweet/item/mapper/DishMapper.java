package com.sweet.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.item.entity.pojo.Category;
import com.sweet.item.entity.pojo.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {


}
