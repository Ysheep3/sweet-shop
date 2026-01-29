package com.sweet.user.service;

import com.sweet.common.result.PageResult;
import com.sweet.user.entity.dto.EmployeeDTO;
import com.sweet.user.entity.dto.EmployeeLoginDTO;
import com.sweet.user.entity.dto.EmployeePageDTO;
import com.sweet.user.entity.dto.RiderLoginDTO;
import com.sweet.user.entity.pojo.Employee;
import com.sweet.user.entity.vo.EmployeeLoginVO;
import com.sweet.user.entity.vo.EmployeeVO;

public interface EmployeeService {
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    PageResult pageQuery(EmployeePageDTO employeePageDTO);

    void save(EmployeeDTO employeeDTO);

    void updateEmployee(EmployeeDTO employeeDTO);

    void startOrStop(Integer status, Long id);

    EmployeeVO getById(Long id);

    EmployeeLoginVO riderLogin(EmployeeLoginDTO employeeLoginDTO);
}
