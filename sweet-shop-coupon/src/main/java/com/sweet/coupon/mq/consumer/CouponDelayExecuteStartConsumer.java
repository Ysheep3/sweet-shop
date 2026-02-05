package com.sweet.coupon.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.sweet.coupon.common.CouponRedisKey;
import com.sweet.coupon.common.CouponStatusEnum;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mq.base.MessageWrapper;
import com.sweet.coupon.mq.event.CouponExecuteStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j(topic = "CouponDelayExecuteStartConsumer")
@RocketMQMessageListener(
        topic = "sweet-shop-coupon-admin-service_coupon-delay_start_topic",
        consumerGroup = "sweet-shop-coupon-admin-service_coupon-delay-start_cg"
)
public class CouponDelayExecuteStartConsumer implements RocketMQListener<MessageWrapper<CouponExecuteStatusEvent>> {

    private final CouponMapper couponMapper;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(MessageWrapper<CouponExecuteStatusEvent> messageWrapper) {

        log.info("[消费者] 优惠卷启用定时执行, 消息体:{}", messageWrapper);
        CouponExecuteStatusEvent event = messageWrapper.getMessage();

        Long couponId = event.getCouponId();
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            return;
        }

//    - 筛掉延后操作后的旧消息
        if (LocalDateTime.now().isBefore(coupon.getStartTime())) {
            log.info("忽略过期消息，couponId={}, msgDeliverTime={}",
                    couponId, event.getDeliverTime());
            return;
        }

        // 筛掉了提前操作后的旧消息
        LambdaUpdateWrapper<Coupon> wrapper = Wrappers.lambdaUpdate(Coupon.class)
                .eq(Coupon::getId, couponId)
                .eq(Coupon::getStatus, CouponStatusEnum.UNAVAILABLE.getCode())
                .set(Coupon::getStatus, CouponStatusEnum.AVAILABLE.getCode());

        int row = couponMapper.update(null, wrapper);

        if (!SqlHelper.retBool(row)) {
            log.info("优惠券已启用，忽略重复消息，couponId={}", couponId);
            return;
        }

        String couponKey = String.format(CouponRedisKey.COUPON_KEY, couponId);
        Map<String, Object> couponMap = BeanUtil.beanToMap(coupon, false, true);
        Map<String, String> actrualCouponMap = couponMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                ));

        List<String> args = new ArrayList<>(actrualCouponMap.size() * 2 + 1);
        actrualCouponMap.forEach((key, value) -> {
            args.add(key);
            args.add(value);
        });
        LocalDateTime endTime = coupon.getEndTime();
        long endTimeMills = endTime
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli();
        String endTimeStr = String.valueOf(endTimeMills / 1000);
        args.add(endTimeStr);
        List<String> keys = Collections.singletonList(couponKey);

        String luaScript = "redis.call('HMSET', KEYS[1], unpack(ARGV, 1, #ARGV -1))" +
                "redis.call('EXPIREAT', KEYS[1], ARGV[#ARGV])";

        stringRedisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                keys,
                args.toArray()
        );

        // 加入“可查询索引”
        stringRedisTemplate.opsForSet().add(CouponRedisKey.COUPON_ENABLE_KEY, couponId.toString());

//        RSet<Long> enabledSet = redissonClient.getSet(CouponRedisKey.COUPON_ENABLE_KEY);
//        enabledSet.add(couponId);

//        RMap<String, Object> redisMap = redissonClient.getMap(redisKey);
//        Map<String, Object> fieldMap = BeanUtil.beanToMap(
//                coupon,
//                new HashMap<>(),
//                CopyOptions.create().setIgnoreNullValue(true)
//        );
//
//        redisMap.putAll(fieldMap);
//
//        long expireSeconds = Duration.between(coupon.getStartTime(), coupon.getEndTime()).getSeconds();
//        if (expireSeconds > 0) {
//            redisMap.expire(expireSeconds, TimeUnit.SECONDS);
//        }
//
//        // 加入“可查询索引”
//        RSet<Long> enabledSet = redissonClient.getSet(CouponRedisKey.COUPON_ENABLE_KEY);
//        enabledSet.add(couponId);

        log.info("优惠券启用完成，couponId={}", couponId);

    }

}


