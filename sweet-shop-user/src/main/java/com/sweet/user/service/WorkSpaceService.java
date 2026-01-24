package com.sweet.user.service;

import com.sweet.api.dto.BusinessDataVO;
import com.sweet.api.dto.DishOverViesVO;
import com.sweet.api.dto.OrderOverViewVO;
import com.sweet.api.dto.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {
    /**
     * 工作台查询今日运营数据
     *
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询订单管理数据
     *
     * @return
     */
    OrderOverViewVO getOverviewOrders();

    /**
     * 套餐总览
     * @return
     */
    SetmealOverViewVO overviewSetmeals();

    /**
     * 菜品总览
     * @return
     */
    DishOverViesVO overviewDishes();
}
