package com.sweet.user.controller;

import com.sweet.user.entity.vo.UserLoginVO;
import com.sweet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserLoginVO login(@RequestBody UserLoginVO requestParam) {
        // return userService.login(requestParam);
        return null;
    }

}
