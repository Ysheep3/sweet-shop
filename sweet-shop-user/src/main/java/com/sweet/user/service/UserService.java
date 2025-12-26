package com.sweet.user.service;

import com.alipay.api.AlipayApiException;
import com.sweet.user.entity.dto.UserLoginDTO;
import com.sweet.user.entity.pojo.User;

public interface UserService {
    User alipayLogin(UserLoginDTO requestParam) throws AlipayApiException;
}
