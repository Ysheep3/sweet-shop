package com.sweet.coupon.common;

import lombok.Data;

@Data
public class CouponRedisKey {
    public final static String COUPON_KEY = "coupon::%s";
    public final static String COUPON_ENABLE_KEY = "coupon::enabled";
    public final static String USER_COUPON_LIMIT_KEY = "user_coupon_limit::%s_%s";
    public final static String USER_COUPON_KEY = "user_coupon_enabled::%s";
    public final static String USER_COUPON_IDEMPOTENT_KEY = "user_coupon_redeem_idempotent::%s:%s";
    public final static String LOCK_USER_COUPON_KEY = "lock_user_coupon::%s";
}
