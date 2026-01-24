package com.sweet.user.service.impl;

import cn.hutool.db.Db;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.api.client.ItemClient;
import com.sweet.api.client.OrderClient;
import com.sweet.api.dto.BusinessDataVO;
import com.sweet.api.dto.DishOverViesVO;
import com.sweet.api.dto.OrderOverViewVO;
import com.sweet.api.dto.SetmealOverViewVO;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.exception.BaseException;
import com.sweet.common.result.Result;
import com.sweet.user.entity.pojo.User;
import com.sweet.user.mapper.UserMapper;
import com.sweet.user.service.WorkSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkSpaceServiceImpl implements WorkSpaceService {
    private final UserMapper userMapper;
    private final OrderClient orderClient;
    private final ItemClient itemClient;

    /**
     * 工作台查询今日运营数据
     *
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime beginTime, LocalDateTime endTime) {
        Long newUserCount = userMapper.selectCount(
                Wrappers.lambdaQuery(User.class)
                        .gt(User::getCreateTime, beginTime));

        Result<BusinessDataVO> orderData = orderClient.getBusinessData(beginTime, endTime);
        if (orderData.getData() == null) {
            throw new BaseException(MessageConstant.GET_ERROR);
        }
        BusinessDataVO vo = orderData.getData();
        vo.setNewUsers(newUserCount.intValue());

        return vo;
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOverviewOrders() {
        Result<OrderOverViewVO> result = orderClient.overviewOrders();
        if (result == null) {
            throw new BaseException(MessageConstant.GET_ERROR);
        }

        return result.getData();
    }

    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Result<SetmealOverViewVO> result = itemClient.overviewSetmeals();
        if (result == null) {
            throw new BaseException(MessageConstant.GET_ERROR);
        }

        return result.getData();
    }

    @Override
    public DishOverViesVO overviewDishes() {
        Result<DishOverViesVO> result = itemClient.overviewDishes();
        if (result == null) {
            throw new BaseException(MessageConstant.GET_ERROR);
        }
        return result.getData();
    }

}
