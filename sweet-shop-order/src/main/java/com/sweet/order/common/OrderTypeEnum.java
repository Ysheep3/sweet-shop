package com.sweet.order.common;

import lombok.Getter;

@Getter
public enum OrderTypeEnum {
    DINEIN(0, "堂食"),
    DELIVERY(1, "外送");
    private final Integer code;
    private final String description;

    OrderTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
