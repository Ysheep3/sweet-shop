package com.sweet.user.common;

import lombok.Data;
import lombok.Getter;

/**
 * JWT 声明常量枚举
 */
@Getter
public enum JwtClaimsEnum {
    
    EMP_ID("adminId", "管理员ID"),
    RIDER_ID("riderId", "外送员ID"),
    USER_ID("userId", "用户ID"),
    PHONE("phone", "手机号"),
    USERNAME("username", "用户名"),
    NAME("name", "姓名");

    private final String claim;
    private final String description;
    
    JwtClaimsEnum(String claim, String description) {
        this.claim = claim;
        this.description = description;
    }

    @Override
    public String toString() {
        return claim;
    }
}