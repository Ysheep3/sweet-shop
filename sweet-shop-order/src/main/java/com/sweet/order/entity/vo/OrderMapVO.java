package com.sweet.order.entity.vo;

import com.sweet.order.entity.pojo.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMapVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String orderNo;

    private String consignee;

    private String phone;

    private String address;

    private BigDecimal amount;

    /** 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 */
    private Integer status;

    private BigDecimal customerLatitude;

    private BigDecimal customerLongitude;
}
