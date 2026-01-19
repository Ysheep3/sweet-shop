package com.sweet.coupon.controller.user;

import com.sweet.common.result.Result;
import com.sweet.coupon.entity.dto.UserCouponClaimDTO;
import com.sweet.coupon.entity.vo.UserCouponVO;
import com.sweet.coupon.service.UserCouponService;
import lombok.Getter;
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
    public Result<Void> claimCoupon(@RequestBody UserCouponClaimDTO requestParam) {
        userCouponService.claimCoupon(requestParam);
        return Result.success();
    }
}
