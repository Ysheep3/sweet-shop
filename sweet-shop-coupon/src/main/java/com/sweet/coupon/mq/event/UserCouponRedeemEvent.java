package com.sweet.coupon.mq.event;

import com.sweet.coupon.entity.dto.UserCouponRedeemDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponRedeemEvent {
    private Coupon coupon;

    private UserCouponRedeemDTO requestParam;

    private Integer receiveCount;

    private Long userId;

    private String idempotent;
}
