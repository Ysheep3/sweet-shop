package com.sweet.order.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家派单执行事件（需要的参数）
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDispatchOrderEvent {
    /**
     * 订单 id
     */
    private Long orderId;

    private String redisKey;

    /**
     * 延时具体时间
     *
     */
    private Long deliverTime;
}
