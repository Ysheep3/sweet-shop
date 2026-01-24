package com.sweet.order.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderPayDTO {
    /** 你系统内部订单号 */
    private String orderNo;

    /** 支付金额（建议后端自己查库校验） */
    private BigDecimal amount;
}
