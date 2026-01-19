package com.sweet.coupon.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long couponId;

    /** 领取次数 */
    private Integer receiveCount;

    /** 领取时间 */
    private LocalDateTime receiveTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime useTime;

    private Long orderId;

    /** 0 未使用，1 已使用，2 已过期 */
    private Integer status;
}
