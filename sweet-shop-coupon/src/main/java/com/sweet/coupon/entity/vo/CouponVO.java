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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    /** 1 满减券，2 折扣券，3 无门槛 */
    private Integer type;

    /** 满 多少 可以减 */
    private BigDecimal conditionAmount;

    /** 减多少 */
    private BigDecimal reduceAmount;

    /** 折扣 */
    private Double discount;

    /** 1 有效，0 无效 */
    private Integer status;

    /** 标签 */
    private List<String> tags;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
