package com.sweet.coupon.service;

import com.sweet.coupon.entity.vo.CouponVO;

import java.util.List;

public interface CouponService {
    /**
     * 用户获取可领取的优惠券列表
     * @return
     */
    List<CouponVO> list();

    /**
     * 通过 id获取 coupon
     *
     * @param id
     * @return CouponVO
     */
    CouponVO getById(Long id);
}
