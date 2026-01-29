package com.sweet.user.controller;

import com.sweet.common.constant.MessageConstant;
import com.sweet.common.exception.LoginException;
import com.sweet.common.result.Result;
import com.sweet.common.utils.JwtUtil;
import com.sweet.user.common.JwtClaimsEnum;
import com.sweet.user.common.JwtProperties;
import com.sweet.user.entity.dto.EmployeeLoginDTO;
import com.sweet.user.entity.dto.RiderLoginDTO;
import com.sweet.user.entity.vo.EmployeeLoginVO;
import com.sweet.user.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController("riderController")
@RequestMapping("/rider")
@RequiredArgsConstructor
public class RiderController {
    private final EmployeeService employeeService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        EmployeeLoginVO employeeLoginVO = employeeService.riderLogin(employeeLoginDTO);

        Map<String, Object> claims = new HashMap<>(); // 自定义负载信息
        claims.put(JwtClaimsEnum.RIDER_ID.getClaim(), employeeLoginVO.getId());

        String token = JwtUtil.createJWT(jwtProperties.getRiderSecretKey(), jwtProperties.getRiderTtl(), claims);
        employeeLoginVO.setToken(token);

        return Result.success(employeeLoginVO);
    }


}
