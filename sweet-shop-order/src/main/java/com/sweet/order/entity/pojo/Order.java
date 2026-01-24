package com.sweet.order.entity.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("orders")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long userId;

    private Long addressId;

    private Long userCouponId;

    private String consignee;

    private String phone;

    private String address;

    private LocalDateTime orderTime;

    private LocalDateTime payTime;

    private BigDecimal amount;

    private Integer payMethod;

    /** 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 */
    private Integer status;

    /** 支付状态 0未支付 1已支付 2退款 */
    private Integer payStatus;

    /** 0 堂食，1 外卖 */
    private Integer orderType;

    private Long deliveryEmployeeId;

    private LocalDateTime deliveryTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private String rejectionReason;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
