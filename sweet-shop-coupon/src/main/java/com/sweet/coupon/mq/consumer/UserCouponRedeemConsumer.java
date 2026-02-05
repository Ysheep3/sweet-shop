package com.sweet.coupon.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.exception.CouponBusinessException;
import com.sweet.coupon.common.CouponRedisKey;
import com.sweet.coupon.common.UserCouponStatusEnum;
import com.sweet.coupon.entity.dto.UserCouponRedeemDTO;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mapper.UserCouponMapper;
import com.sweet.coupon.mq.base.MessageWrapper;
import com.sweet.coupon.mq.event.UserCouponExecuteStatusEvent;
import com.sweet.coupon.mq.event.UserCouponRedeemEvent;
import com.sweet.coupon.mq.producer.UserCouponDelayExecuteEndProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

@Slf4j(topic = "UserCouponRedeemConsumer")
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "sweet-shop-coupon_user-coupon-redeem-service_topic",
        consumerGroup = "sweet-shop-coupon_user-coupon-redeem-service_cg")
public class UserCouponRedeemConsumer implements RocketMQListener<MessageWrapper<UserCouponRedeemEvent>> {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponDelayExecuteEndProducer endProducer;
    private final RedissonClient redissonClient;


    @Override
    public void onMessage(MessageWrapper<UserCouponRedeemEvent> messageWrapper) {
        UserCouponRedeemEvent message = messageWrapper.getMessage();
        // 避免日志/调试器对复杂对象做 toString/序列化时触发递归导致 StackOverflowError
        log.info("[消费者] 用户兑换优惠卷, 开始执行, keys={}, idempotent={}, userId={}, couponId={}",
                messageWrapper.getKeys(),
                message != null ? message.getIdempotent() : null,
                message != null ? message.getUserId() : null,
                message != null && message.getCoupon() != null ? message.getCoupon().getId() : null);

        if (message == null) {
            log.warn("消息体为空，跳过消费，keys={}", messageWrapper.getKeys());
            return;
        }

        // 保证幂等性
        String idempotent = message.getIdempotent();
        Long userId = message.getUserId();
        String idempotentKey = String.format(CouponRedisKey.USER_COUPON_IDEMPOTENT_KEY,
                userId, idempotent);

        Boolean isFirstProcess = stringRedisTemplate.execute(
                new DefaultRedisScript<>(
                        "return redis.call('SET', KEYS[1], '1', 'NX', 'EX', ARGV[1])",
                        Boolean.class
                ),
                Collections.singletonList(idempotentKey),
                "86400" // 24小时
        );

        if (!isFirstProcess) {
            log.info("消息已处理，跳过重复消费，idempotent:{}", idempotent);
            return;
        }

        Coupon coupon = message.getCoupon();
        Long couponId = coupon.getId();
        UserCouponRedeemDTO requestParam = message.getRequestParam();
        int row = couponMapper.update(
                null,
                Wrappers.lambdaUpdate(Coupon.class)
                        .setSql("stock = stock - 1")
                        .eq(Coupon::getId, couponId)
                        .ge(Coupon::getStock, 1)
        );

        if (!SqlHelper.retBool(row)) {
            // 库存不足
            throw new CouponBusinessException(MessageConstant.COUPON_STOCK_NOT_ENOUGH);
        }

        LocalDateTime now = LocalDateTime.now();

        UserCoupon userCoupon = BeanUtil.toBean(requestParam, UserCoupon.class);
        userCoupon.setStatus(UserCouponStatusEnum.UNUSED.getCode());
        userCoupon.setUserId(userId);
        userCoupon.setReceiveTime(now);
        // 可选：这里也可以记录累计领取次数 receiveCount，当前按“本条记录代表一次领取”记为 1
        userCoupon.setReceiveCount(1);

        userCouponMapper.insert(userCoupon);

        long mills = now.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
        String userCouponListKey = String.format(CouponRedisKey.USER_COUPON_KEY, userId);
        String userCouponItemValue = StrUtil.builder().append(requestParam.getCouponId()).append("_").append(userCoupon.getId()).toString();

        // Spring Data Redis 2.4.3 在 JDK17 下可能触发 DefaultedRedisConnection.zAdd 递归导致 StackOverflowError
        // 这里改用 Redisson 操作 ZSet 规避该问题
        RScoredSortedSet<String> zset = redissonClient.getScoredSortedSet(userCouponListKey);
        zset.add((double) mills, userCouponItemValue);

        // 再次校验是否写入成功（提高成功率）
        if (zset.getScore(userCouponItemValue) == null) {
            zset.add((double) mills, userCouponItemValue);
        }


        sendDelayMessage(userCoupon);
    }

    private void sendDelayMessage(UserCoupon userCoupon) {
        LocalDateTime endTime;
        endTime = userCoupon.getEndTime();
        long endTimeMills = endTime.atZone(ZoneId.of("Asia/Shanghai"))
                .toInstant()
                .toEpochMilli();

        UserCouponExecuteStatusEvent event = UserCouponExecuteStatusEvent.builder()
                .userCouponId(userCoupon.getId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCouponId())
                .deliverTime(endTimeMills)
                .build();

        SendResult sendResult = endProducer.sendMessage(event);
        if (!sendResult.getSendStatus().name().equals("SEND_OK")) {
            log.warn("用户优惠卷延迟结束消息发送失败, 消息体:{}", event);
        }
    }
}
