package com.sweet.coupon.controller.admin;

import com.sweet.common.result.PageResult;
import com.sweet.common.result.Result;
import com.sweet.coupon.entity.dto.CouponDTO;
import com.sweet.coupon.entity.dto.CouponPageDTO;
import com.sweet.coupon.entity.vo.AdminCouponVO;
import com.sweet.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminCouponController")
@RequestMapping("/coupon/admin")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/page")
    public Result<PageResult> page(CouponPageDTO couponPageDTO) {
        PageResult pageResult = couponService.page(couponPageDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<AdminCouponVO> getById(@PathVariable Long id) {
        AdminCouponVO vo = couponService.getById(id);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> create(@RequestBody CouponDTO couponDTO) {
        couponService.create(couponDTO);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CouponDTO couponDTO) {
        couponService.update(couponDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result<Void> startOrStop(Long id, @PathVariable Integer status) {
        couponService.startOrStop(id, status);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete(@RequestBody List<Long> ids) {
        couponService.delete(ids);
        return Result.success();
    }
}
