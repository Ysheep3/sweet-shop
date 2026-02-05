package com.sweet.coupon.common;

import lombok.Getter;

@Getter
public enum CouponTypeEnum {
    FULL_REDUCE(1, "满减券"),
    DISCOUNT(2, "折扣券"),
    REDUCE(3, "无门槛");

    private final Integer code;
    private final String description;

    CouponTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
