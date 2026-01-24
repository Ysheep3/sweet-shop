package com.sweet.user.controller;

import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.common.utils.JwtUtil;
import com.sweet.user.common.JwtClaimsEnum;
import com.sweet.user.common.JwtProperties;
import com.sweet.user.entity.dto.EmployeeDTO;
import com.sweet.user.entity.dto.EmployeeLoginDTO;
import com.sweet.user.entity.dto.EmployeePageDTO;
import com.sweet.user.entity.pojo.Employee;
import com.sweet.user.entity.vo.EmployeeLoginVO;
import com.sweet.user.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("employeeController")
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        EmployeeLoginVO employeeLoginVO = employeeService.login(employeeLoginDTO);

        Map<String, Object> claims = new HashMap<>(); // 自定义负载信息
        claims.put(JwtClaimsEnum.EMP_ID.getClaim(), employeeLoginVO.getId());

        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
        employeeLoginVO.setToken(token);

        return Result.success(employeeLoginVO);
    }


    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageDTO employeePageDTO) {
        PageResult pageResult = employeeService.pageQuery(employeePageDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        employee.setPassword("*****");

        return Result.success(employee);
    }

    @PutMapping
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.updateEmployee(employeeDTO);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }
}
