package com.sweet.coupon.mq.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 结束优惠卷执行事件（需要的参数）
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponExecuteStatusEvent {
    /**
     * 结束任务id
     */
    private Long couponId;

    /**
     * 延时具体时间
     *
     */
    private Long deliverTime;
}
