package com.sweet.user.controller;

import com.alipay.api.AlipayApiException;
import com.sweet.user.common.JwtClaimsEnum;
import com.sweet.user.common.JwtProperties;
import com.sweet.user.entity.dto.UserLoginDTO;
import com.sweet.user.entity.pojo.User;
import com.sweet.user.entity.vo.UserLoginVO;
import com.sweet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sweet.shop.common.result.Result;
import sweet.shop.common.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RestController("userController")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO requestParam) throws AlipayApiException {
        UserLoginVO userLoginVO = userService.alipayLogin(requestParam);
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsEnum.USER_ID.getClaim(),userLoginVO.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        userLoginVO.setToken(token);

        return Result.success(userLoginVO);
    }

}
