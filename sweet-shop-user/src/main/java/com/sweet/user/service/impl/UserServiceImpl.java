package com.sweet.user.service.impl;

import com.sweet.user.mapper.UserMapper;
import com.sweet.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {
    private final UserMapper userMapper;
}
