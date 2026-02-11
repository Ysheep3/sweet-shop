package com.sweet.order.entity.dto;

import lombok.Data;

@Data
public class RiderOrderStatDTO {
    private Long riderId;
    private Integer totalOrderCount;
    private Integer validOrderCount;
}