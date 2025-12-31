package com.sweet.user.service;

import com.alipay.api.AlipayApiException;
import com.sweet.user.entity.dto.UserLoginDTO;
import com.sweet.user.entity.vo.UserLoginVO;

public interface UserService {
    UserLoginVO alipayLogin(UserLoginDTO requestParam) throws AlipayApiException;
}
