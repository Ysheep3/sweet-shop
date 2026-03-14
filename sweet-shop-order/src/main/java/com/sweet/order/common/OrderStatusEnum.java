package com.sweet.order.common;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    // 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
    PENDING_PAYMENT(1, "待付款"),
    PENDING_ACCEPTANCE(2, "待接单"),
    ACCEPTED(3, "待出餐"),
    IN_DELIVERY(4, "派送中"),
    PENDING_PICKUP(7, "待取餐"),
    PICKING_UP(8, "取餐中"),
    COMPLETED(5, "已完成"),
    CANCELED(6, "已取消");

    private final Integer code;
    private final String description;

    OrderStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
