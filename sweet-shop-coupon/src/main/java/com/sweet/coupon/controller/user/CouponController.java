package com.sweet.coupon.controller.user;

import com.sweet.common.result.Result;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.vo.CouponVO;
import com.sweet.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("CouponController")
@RequestMapping("/coupon/user")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/list")
    public Result<List<CouponVO>> list() {
        List<CouponVO> couponVOS= couponService.list();
        return Result.success(couponVOS);
    }

    @GetMapping("/{id}")
    public Result<CouponVO> getById(@PathVariable Long id) {
        CouponVO couponVO = couponService.getById(id);
        return Result.success(couponVO);
    }
}
