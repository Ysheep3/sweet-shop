package com.sweet.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.constant.PasswordConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.BaseException;
import com.sweet.common.exception.LoginException;
import com.sweet.common.result.PageResult;
import com.sweet.user.common.EmployeeRoleEnum;
import com.sweet.user.entity.dto.EmployeeDTO;
import com.sweet.user.entity.dto.EmployeeLoginDTO;
import com.sweet.user.entity.dto.EmployeePageDTO;
import com.sweet.user.entity.dto.RiderLoginDTO;
import com.sweet.user.entity.pojo.Employee;
import com.sweet.user.entity.vo.EmployeeLoginVO;
import com.sweet.user.entity.vo.EmployeeVO;
import com.sweet.user.mapper.EmployeeMapper;
import com.sweet.user.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMapper employeeMapper;
    /**
     * 管理员登录
     *
     * @return
     */
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, employeeLoginDTO.getUsername())
                .eq(Employee::getPassword, password);
        Employee employee = employeeMapper.selectOne(wrapper);

        return BeanUtil.toBean(employee, EmployeeLoginVO.class);
    }

    /**
     * 员工分页查询
     *
     * @param employeePageDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageDTO employeePageDTO) {
        IPage<Employee> page = new Page<>(employeePageDTO.getPage(),employeePageDTO.getPageSize());

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (employeePageDTO.getName() != null) {
            wrapper.like(Employee::getName,employeePageDTO.getName());
        }

        List<Employee> employeeList = employeeMapper.selectPage(page, wrapper).getRecords();

        List<EmployeeVO> vos = BeanUtil.copyToList(employeeList, EmployeeVO.class);

        return new PageResult(page.getTotal(),vos);
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);
        String password = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes());
        employee.setPassword(password);

       // employee.setCreateTime(LocalDateTime.now());
        employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insert(employee);
    }

    /**
     * 修改员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);
        //setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.updateById(employee);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                //.updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();

        employeeMapper.updateById(employee);
    }

    @Override
    public EmployeeVO getById(Long id) {
        if (id == null) {
            throw new BaseException(MessageConstant.DO_ERROR);
        }
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new BaseException(MessageConstant.EMPLOYEE_IS_NULL);
        }

        return BeanUtil.toBean(employee, EmployeeVO.class);
    }

    @Override
    public EmployeeLoginVO riderLogin(EmployeeLoginDTO employeeLoginDTO) {
        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, employeeLoginDTO.getUsername())
                .eq(Employee::getPassword, password)
                .eq(Employee::getRole, EmployeeRoleEnum.Rider.getCode());


        Employee employee = employeeMapper.selectOne(wrapper);

        if (employee == null) {
            throw new LoginException(MessageConstant.USER_LOGIN_ERROR_FOR_USERNAME_OR_PWD);
        }
        return BeanUtil.toBean(employee, EmployeeLoginVO.class);
    }
}
