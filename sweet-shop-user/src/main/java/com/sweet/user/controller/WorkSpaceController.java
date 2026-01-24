package com.sweet.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sweet.api.dto.BusinessDataVO;
import com.sweet.api.dto.DishOverViesVO;
import com.sweet.api.dto.OrderOverViewVO;
import com.sweet.api.dto.SetmealOverViewVO;
import com.sweet.common.result.Result;
import com.sweet.user.service.WorkSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
@RequiredArgsConstructor
public class WorkSpaceController {
    private final WorkSpaceService workSpaceService;

    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        LocalDate today = LocalDate.now();
        LocalDateTime beginTime = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(today, LocalTime.MAX);
        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(beginTime, endTime);

        return Result.success(businessDataVO);
    }

    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        SetmealOverViewVO vo = workSpaceService.overviewSetmeals();
        return Result.success(vo);
    }

    @GetMapping("/overviewDishes")
    public Result<DishOverViesVO> overviewDishes() {
        DishOverViesVO vo = workSpaceService.overviewDishes();
        return Result.success(vo);
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders() {
        OrderOverViewVO orderOverViewVO = workSpaceService.getOverviewOrders();
        return Result.success(orderOverViewVO);
    }
}
