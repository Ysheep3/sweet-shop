package com.sweet.user.service.impl;

import com.sweet.user.mapper.EmployeeMapper;
import com.sweet.user.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMapper employeeMapper;
}
