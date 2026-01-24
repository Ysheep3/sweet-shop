package com.sweet.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.item.entity.pojo.Setmeal;
import com.sweet.item.entity.pojo.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {

    void insertBatchs(@Param("list") List<SetmealDish> setmealDishes);

    void deleteBySetmealIds(List<Long> setmealIds);
}
