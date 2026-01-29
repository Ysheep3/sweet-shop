package com.sweet.coupon.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminCouponVO extends CouponVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int stock;

    private int limitPerUser;
}
