package com.sweet.coupon.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.coupon.common.CouponStatusEnum;
import com.sweet.coupon.common.UserCouponStatusEnum;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.entity.pojo.UserCoupon;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mapper.UserCouponMapper;
import com.sweet.coupon.mq.base.MessageWrapper;
import com.sweet.coupon.mq.event.CouponExecuteStatusEvent;
import com.sweet.coupon.mq.event.UserCouponExecuteStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j(topic = "UserCouponDelayExecuteEndConsumer")
@RocketMQMessageListener(
        topic = "sweet-shop-coupon-user-service_coupon-delay_end_topic",
        consumerGroup = "sweet-shop-coupon-user-service_coupon-delay-end_cg"
)
public class UserCouponDelayExecuteEndConsumer implements RocketMQListener<MessageWrapper<UserCouponExecuteStatusEvent>> {

    private final UserCouponMapper userCouponMapper;

    @Override
    public void onMessage(MessageWrapper<UserCouponExecuteStatusEvent> messageWrapper) {

        log.info("[消费者] 用户优惠卷结束定时执行, 消息体:{}", messageWrapper);
        UserCouponExecuteStatusEvent event = messageWrapper.getMessage();

        UserCoupon userCoupon = userCouponMapper.selectById(event.getUserCouponId());
        if (userCoupon == null) {
            return;
        }

//  如果当前时间 < 数据库中的 endTime
//    - 筛掉延后操作后的旧消息
        if (LocalDateTime.now().isBefore(userCoupon.getEndTime())) {
            log.info("忽略过期消息，userCouponId={}, msgDeliverTime={}",
                    userCoupon.getId(), event.getDeliverTime());
            return;
        }

        // 筛掉了提前操作后的旧消息
        LambdaUpdateWrapper<UserCoupon> wrapper = Wrappers.lambdaUpdate(UserCoupon.class)
                .eq(UserCoupon::getId, userCoupon.getId())
                .eq(UserCoupon::getUserId, event.getUserId())
                .eq(UserCoupon::getStatus, UserCouponStatusEnum.UNUSED.getCode())
                .set(UserCoupon::getStatus, UserCouponStatusEnum.EXPIRED.getCode());

        userCouponMapper.update(null, wrapper);

    }

}


