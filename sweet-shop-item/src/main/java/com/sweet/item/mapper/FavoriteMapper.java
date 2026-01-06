package com.sweet.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.item.entity.pojo.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
