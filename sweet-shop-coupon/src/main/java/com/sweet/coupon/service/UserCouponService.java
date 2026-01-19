package com.sweet.coupon.service;


import com.sweet.coupon.entity.dto.UserCouponClaimDTO;
import com.sweet.coupon.entity.vo.UserCouponVO;

import java.util.List;

public interface UserCouponService {
    List<UserCouponVO> listByStatus(Integer status);

    void claimCoupon(UserCouponClaimDTO requestParam);
}
