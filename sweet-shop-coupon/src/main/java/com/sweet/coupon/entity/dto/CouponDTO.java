package com.sweet.coupon.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class CouponDTO  {
    private String name;

    /** 1 满减券，2 折扣券，3 无门槛 */
    private Integer type;

    /** 库存 */
    private Integer stock;

    /** 满 多少 可以减 */
    private BigDecimal conditionAmount;

    /** 减多少 */
    private BigDecimal reduceAmount;

    /** 折扣 */
    private Double discount;

    /** 发放总量 */
    private Integer totalQuantity;

    /** 使用数量 */
    private Integer usedQuantity;

    /** 1 有效，0 无效 */
    private Integer status;

    /** 标签 */
    private List<String> tags;

    /** 每人限领张数 */
    private Integer limitPerUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
