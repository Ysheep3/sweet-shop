package com.sweet.order.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPayVO {
    private String id;

    private BigDecimal orderAmount;

    private String orderNo;

    private String consignee;
}
