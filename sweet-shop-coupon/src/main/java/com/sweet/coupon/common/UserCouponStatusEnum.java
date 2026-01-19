package com.sweet.coupon.common;

import lombok.Getter;

@Getter
public enum UserCouponStatusEnum {
    UNUSED(0, "未使用"),
    USED(1, "已使用"),
    EXPIRED(2, "已过期");

    private final Integer code;
    private final String description;

    UserCouponStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
