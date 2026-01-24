package com.sweet.order.entity.vo;

import com.sweet.order.entity.pojo.Order;
import com.sweet.order.entity.pojo.OrderDetail;
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
public class OrderVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String orderNo;

    private String addressId;

    private String userCouponId;

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

    private List<OrderDetail> orderDetails;

    private String orderDishes;
}
