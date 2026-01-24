package com.sweet.order.task;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sweet.order.common.OrderStatusEnum;
import com.sweet.order.entity.pojo.Order;
import com.sweet.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理超出付款时间的订单
     */
    @Scheduled(cron = "0 0/5 * * * *")   // 每五分钟处理
    public void processTimeOutOrder(){
        log.info("开始处理超时订单{}:", new Date());
        // 查询 15 分之前之前的待付款订单
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusMinutes(15);

        List<Order> orders = new LambdaQueryChainWrapper<>(orderMapper)
                .lt(Order::getOrderTime, before)
                .eq(Order::getStatus, OrderStatusEnum.PENDING_PAYMENT.getCode())
                .list();

        if (orders !=null && !orders.isEmpty()) {
            for (Order order : orders) {
                order.setStatus(OrderStatusEnum.CANCELED.getCode());
                order.setCancelTime(now);
                order.setCancelReason("订单超时");

                orderMapper.updateById(order);
            }
        }
    }

    /**
     * 处理派送中未点击完成的订单
     *
     */
    @Scheduled(cron = "0 0 0 * * ? ")  // 每天凌晨12点
    public void processDeliveryOrder(){
        log.info("处理派送中的订单{}",new Date());

        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = new LambdaQueryChainWrapper<>(orderMapper)
                .lt(Order::getOrderTime, now)
                .eq(Order::getStatus, OrderStatusEnum.IN_DELIVERY.getCode())
                .list();

        for (Order order : orders) {
            order.setStatus(OrderStatusEnum.COMPLETED.getCode());

            orderMapper.updateById(order);
        }
    }
}
