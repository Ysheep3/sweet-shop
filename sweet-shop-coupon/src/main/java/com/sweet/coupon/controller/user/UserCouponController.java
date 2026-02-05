package com.sweet.coupon.controller.user;

import com.sweet.common.result.Result;
import com.sweet.coupon.entity.dto.UserCouponRedeemDTO;
import com.sweet.coupon.entity.vo.UserCouponVO;
import com.sweet.coupon.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userCouponController")
@RequestMapping("/user-coupon/user")
@RequiredArgsConstructor
public class UserCouponController {
    private final UserCouponService userCouponService;

    @GetMapping("/list/{status}")
    public Result<List<UserCouponVO>> list(@PathVariable Integer status) {
        List<UserCouponVO> userCouponVOS = userCouponService.listByStatus(status);
        return Result.success(userCouponVOS);
    }

    @PostMapping("/claim")
    public Result<Void> redeemCoupon(@RequestBody UserCouponRedeemDTO requestParam) {
        userCouponService.redeemCoupon(requestParam);
        return Result.success();
    }

    @PostMapping("/use/{id}")
    public Result<Void> useCoupon(@PathVariable Long id) {
        userCouponService.useCoupon(id);
        return Result.success();
    }
}
