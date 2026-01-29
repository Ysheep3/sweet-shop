package com.sweet.user.common;

import lombok.Getter;


@Getter
public enum EmployeeRoleEnum {
    ADMIN(0, "管理员"),
    Rider(1, "外送员"),
    OTHER(2, "其他");

    private final Integer code;
    private final String description;

    EmployeeRoleEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}