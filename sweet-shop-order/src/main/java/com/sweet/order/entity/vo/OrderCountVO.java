package com.sweet.order.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCountVO {
    private Long pendingPayment;// pendingPayment

    private Long toBeConfirmed;// toBeConfirmed

    private Long confirmed;// confirmed

    private Long deliveryInProgress;// deliveryInProgress
}
