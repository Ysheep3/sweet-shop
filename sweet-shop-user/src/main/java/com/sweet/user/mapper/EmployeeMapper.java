package com.sweet.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.user.entity.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    List<Employee> selectByIds(@Param("ids") List<Long> ids);
}
