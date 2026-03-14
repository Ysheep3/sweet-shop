package com.sweet.user.service;

import com.sweet.common.result.PageResult;
import com.sweet.user.entity.dto.*;
import com.sweet.user.entity.pojo.Employee;
import com.sweet.user.entity.vo.EmployeeLoginVO;
import com.sweet.user.entity.vo.EmployeeVO;

import java.util.List;

public interface EmployeeService {
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    PageResult pageQuery(EmployeePageDTO employeePageDTO);

    void save(EmployeeDTO employeeDTO);

    void updateEmployee(EmployeeDTO employeeDTO);

    void startOrStop(Integer status, Long id);

    EmployeeVO getById(Long id);

    EmployeeLoginVO riderLogin(EmployeeLoginDTO employeeLoginDTO);

    void getRiderLocation(RiderLocationDTO riderLocationDTO);

    List<Long> getRiderIds();
}
