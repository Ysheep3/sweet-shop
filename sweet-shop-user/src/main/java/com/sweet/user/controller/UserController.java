package com.sweet.user.controller;

import com.alipay.api.AlipayApiException;
import com.sweet.user.entity.dto.UserLoginDTO;
import com.sweet.user.entity.pojo.User;
import com.sweet.user.entity.vo.UserLoginVO;
import com.sweet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/userController")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public UserLoginVO login(@RequestBody UserLoginDTO requestParam) throws AlipayApiException {
        User user = userService.alipayLogin(requestParam);
        return UserLoginVO.builder()
                .id(user.getId())
                .openId(user.getOpenId())
                .build();
    }

}
