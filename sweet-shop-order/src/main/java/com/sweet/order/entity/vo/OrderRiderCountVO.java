package com.sweet.order.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRiderCountVO {
    private Integer waitCount;

    private Integer deliveryCount;

    private Integer completedCount;

    private Integer todayFinished;
}
