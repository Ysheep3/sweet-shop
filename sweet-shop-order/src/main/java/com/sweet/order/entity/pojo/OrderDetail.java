package com.sweet.order.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long orderId;

    private Long dishId;

    private Long setmealId;

    private String name;

    private String image;

    private Integer number;

    private BigDecimal amount;
}
