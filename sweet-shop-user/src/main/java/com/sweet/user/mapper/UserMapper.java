package com.sweet.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.user.entity.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
