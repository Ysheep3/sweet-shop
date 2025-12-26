package com.sweet.user.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.user.common.AlipayProperties;
import com.sweet.user.entity.dto.UserLoginDTO;
import com.sweet.user.entity.pojo.User;
import com.sweet.user.mapper.UserMapper;
import com.sweet.user.service.UserService;
import com.sweet.user.utils.AlipayClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {
    private final UserMapper userMapper;
    private final AlipayClientFactory alipayClientFactory;

    @Override
    public User alipayLogin(UserLoginDTO requestParam) throws AlipayApiException {
        AlipaySystemOauthTokenResponse response = getResponse(requestParam);
        String openId = response.getOpenId();

        if (openId == null) {
            throw new RuntimeException("登录失败");
        }

        LambdaQueryWrapper<User> query = Wrappers.lambdaQuery(User.class).eq(User::getOpenId, openId);
        User user = userMapper.selectOne(query);

        if (user == null) {
            AlipayClient alipayClient = alipayClientFactory.getAlipayClient();
            AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse userInfoResponse = alipayClient.execute(request, response.getAccessToken());

            user = User.builder()
                    .openId(openId)
                    .nickname(userInfoResponse.getNickName())
                    .avatar(userInfoResponse.getAvatar())
                    .status(1)
                    .build();

            userMapper.insert(user);
        }

        return user;
    }

    private AlipaySystemOauthTokenResponse getResponse(UserLoginDTO requestParam) throws AlipayApiException {
        AlipayClient alipayClient = alipayClientFactory.getAlipayClient();
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();

        request.setGrantType("authorization_code");
        request.setCode(requestParam.getCode());

        AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }

        return response;
    }
}
