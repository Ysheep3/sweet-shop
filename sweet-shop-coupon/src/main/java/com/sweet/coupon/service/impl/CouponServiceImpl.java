package com.sweet.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.common.context.BaseContext;
import com.sweet.coupon.common.CouponStatusEnum;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.entity.vo.CouponVO;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mapper.UserCouponMapper;
import com.sweet.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<CouponVO> list() {

        List<Coupon> coupons = couponMapper.selectList(
                Wrappers.lambdaQuery(Coupon.class)
                        .eq(Coupon::getStatus, CouponStatusEnum.AVAILABLE.getCode())
                        .le(Coupon::getStartTime, LocalDateTime.now())
                        .ge(Coupon::getEndTime, LocalDateTime.now())
        );

        if (CollUtil.isEmpty(coupons)) {
            return List.of();
        }

        // 查询用户已领取的优惠券
        List<UserCoupon> userCoupons = userCouponMapper.selectList(
                Wrappers.lambdaQuery(UserCoupon.class)
                        .eq(UserCoupon::getUserId, BaseContext.getCurrentId())
        );

        Map<Long, Integer> receiveCountMap = userCoupons.stream()
                .collect(Collectors.groupingBy(
                        UserCoupon::getCouponId,
                        Collectors.summingInt(UserCoupon::getReceiveCount)
                ));


        // 过滤已领满的优惠券
        coupons = coupons.stream()
                .filter(coupon -> {
                    Integer receiveCount = receiveCountMap.get(coupon.getId());
                    return receiveCount == null
                            || receiveCount < coupon.getLimitPerUser();
                })
                .toList();

        return coupons.stream().map(coupon -> {
            CouponVO couponVO = BeanUtil.toBean(coupon, CouponVO.class);
            couponVO.setTags(StrUtil.splitTrim(coupon.getTags(), ","));
            return couponVO;
        }).toList();
    }


    @Override
    public CouponVO getById(Long id) {
        return null;
    }
}
