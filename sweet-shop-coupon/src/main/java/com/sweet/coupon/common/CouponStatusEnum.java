package com.sweet.coupon.common;

import lombok.Getter;

@Getter
public enum CouponStatusEnum {
    AVAILABLE(1, "可用"),
    UNAVAILABLE(0, "不可用");

    private final Integer code;
    private final String description;

    CouponStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
