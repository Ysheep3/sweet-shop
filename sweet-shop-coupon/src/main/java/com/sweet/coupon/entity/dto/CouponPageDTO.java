package com.sweet.coupon.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponPageDTO {
    private String name;
    private Integer type;
    private Integer status;
    private int page;  // 页码
    private int pageSize;  // 每页个数
}
