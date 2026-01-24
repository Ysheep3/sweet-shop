package com.sweet.order.common;

import lombok.Getter;

@Getter
public enum OrderPayStatusEnum {
    UN_PAID(0, "未支付"),
    PAID(1, "已支付"),
    REFUND(2, "退款");

    private final Integer code;
    private final String description;

    OrderPayStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
