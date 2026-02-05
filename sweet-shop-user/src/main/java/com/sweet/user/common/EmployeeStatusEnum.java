package com.sweet.user.common;

import lombok.Getter;


@Getter
public enum EmployeeStatusEnum {
    ENABLE(1, "启用"),
    UNABLE(0, "禁用");

    private final Integer code;
    private final String description;

    EmployeeStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}