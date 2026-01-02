package com.sweet.item.common;

import lombok.Getter;

@Getter
public enum ItemStatusEnum {
    ENABLED(1, "启动"),
    DISABLED(0, "停止");

    private final int code;
    private final String description;

    ItemStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
