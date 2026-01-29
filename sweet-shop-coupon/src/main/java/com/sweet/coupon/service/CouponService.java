package com.sweet.coupon.service;

import com.sweet.common.result.PageResult;
import com.sweet.coupon.entity.dto.CouponDTO;
import com.sweet.coupon.entity.dto.CouponPageDTO;
import com.sweet.coupon.entity.vo.AdminCouponVO;
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
    AdminCouponVO getById(Long id);

    /**
     * 优惠券分页查询
     * @param couponPageDTO
     * @return
     */
    PageResult page(CouponPageDTO couponPageDTO);

    /**
     * 创建优惠券
     * @param couponDTO
     */
    void create(CouponDTO couponDTO);

    /**
     * 修改优惠券
     * @param couponDTO
     */
    void update(CouponDTO couponDTO);

    /**
     * 启用或停用优惠券
     *
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);

    /**
     * 删除优惠券
     * @param ids
     */
    void delete(List<Long> ids);
}
