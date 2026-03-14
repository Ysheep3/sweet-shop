package com.sweet.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.constant.PasswordConstant;
import com.sweet.common.context.BaseContext;
import com.sweet.common.exception.BaseException;
import com.sweet.common.exception.LoginException;
import com.sweet.common.result.PageResult;
import com.sweet.user.common.EmployeeRoleEnum;
import com.sweet.user.common.EmployeeStatusEnum;
import com.sweet.user.entity.dto.*;
import com.sweet.user.entity.pojo.Employee;
import com.sweet.user.entity.vo.EmployeeLoginVO;
import com.sweet.user.entity.vo.EmployeeVO;
import com.sweet.user.mapper.EmployeeMapper;
import com.sweet.user.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMapper employeeMapper;
    private final StringRedisTemplate stringRedisTemplate;

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
        IPage<Employee> page = new Page<>(employeePageDTO.getPage(), employeePageDTO.getPageSize());

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (employeePageDTO.getName() != null) {
            wrapper.like(Employee::getName, employeePageDTO.getName());
        }

        List<Employee> employeeList = employeeMapper.selectPage(page, wrapper).getRecords();

        List<EmployeeVO> vos = BeanUtil.copyToList(employeeList, EmployeeVO.class);

        return new PageResult(page.getTotal(), vos);
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
        employee.setUsername(employeeDTO.getPhone());
        employee.setPassword(password);

        // employee.setCreateTime(LocalDateTime.now());
        employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        Employee employeeDB = employeeMapper.selectOne(
                Wrappers.lambdaQuery(Employee.class)
                        .eq(Employee::getPhone, employeeDTO.getPhone())
        );

        if (employeeDB != null) {
            throw new LoginException("该手机号已经注册过了");
        }

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
                .eq(Employee::getRole, EmployeeRoleEnum.Rider.getCode())
                .eq(Employee::getStatus, EmployeeStatusEnum.ENABLE.getCode());


        Employee employee = employeeMapper.selectOne(wrapper);

        if (employee == null) {
            throw new LoginException(MessageConstant.USER_LOGIN_ERROR_FOR_USERNAME_OR_PWD);
        }
        return BeanUtil.toBean(employee, EmployeeLoginVO.class);
    }

    @Override
    public void getRiderLocation(RiderLocationDTO riderLocationDTO) {
        String longitude = riderLocationDTO.getLongitude();
        String latitude = riderLocationDTO.getLatitude();

        if (StrUtil.isBlank(longitude) || StrUtil.isBlank(latitude)) {
            throw new BaseException("定位数据异常");
        }
        String key = "rider:location:" + BaseContext.getCurrentId();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = LocalDateTime.now().format(formatter);

        Map<String, String> map = new HashMap<>();
        map.put("longitude", longitude);
        map.put("latitude", latitude);
        map.put("time", time);

        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, 30, TimeUnit.SECONDS);

    }

    @Override
    public List<Long> getRiderIds() {
        List<Employee> employees = employeeMapper.selectList(
                Wrappers.lambdaQuery(Employee.class)
                        .eq(Employee::getRole, EmployeeRoleEnum.Rider.getCode())
                        .eq(Employee::getStatus, EmployeeStatusEnum.ENABLE.getCode())
        );

        return employees.stream()
                .map(Employee::getId)
                .toList();
    }


}
