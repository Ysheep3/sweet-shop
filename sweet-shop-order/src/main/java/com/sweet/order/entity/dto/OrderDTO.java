package com.sweet.order.entity.dto;

import com.sweet.api.dto.ShoppingCartVO;
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
@Builder
public class OrderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long addressId;

    private String address;

    private UserCouponDTO userCoupon;

    private String consignee;

    private String phone;

    private LocalDateTime orderTime;

    private LocalDateTime payTime;

    private BigDecimal amount;

    private Integer payMethod;

    /** 1 待支付，2 已支付，3 已取消，4 已完成 */
    private Integer status;

    /** 0 堂食，1 外卖 */
    private Integer orderType;

    private Long deliveryEmployeeId;

    private String remark;

    private String rejectionReason;

    private String cancelReason;

    private LocalDateTime deliveryTime;

    private List<ShoppingCartVO> cartItems;
}
