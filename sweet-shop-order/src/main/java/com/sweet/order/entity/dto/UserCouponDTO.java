package com.sweet.order.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCouponDTO {
    private Long id;

    private Long couponId;

    /** 1 满减券，2 折扣券，3 无门槛 */
    private Integer type;

    /** 满 多少 可以减 */
    private BigDecimal conditionAmount;

    /** 减多少 */
    private BigDecimal reduceAmount;

    /** 折扣 */
    private Double discount;

    /** 领取时间 */
    private LocalDateTime receiveTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime useTime;

    /** 0 未使用，1 已使用，2 已过期 */
    private Integer status;

    private String name;
}
