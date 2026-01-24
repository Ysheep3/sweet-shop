package com.sweet.user.common;

import lombok.Getter;


@Getter
public enum AddressDefaultEnum {
    DEFAULT(1, "默认地址"),
    NOT_DEFAULT(0, "非默认地址");

    private final Integer code;
    private final String description;

    AddressDefaultEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}