package com.sweet.coupon.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sweet.coupon.common.CouponStatusEnum;
import com.sweet.coupon.entity.pojo.Coupon;
import com.sweet.coupon.mapper.CouponMapper;
import com.sweet.coupon.mq.base.MessageWrapper;
import com.sweet.coupon.mq.event.CouponExecuteStatusEvent;
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
@Slf4j(topic = "CouponDelayExecuteEndConsumer")
@RocketMQMessageListener(
        topic = "sweet-shop-coupon-admin-service_coupon-delay_end_topic",
        consumerGroup = "sweet-shop-coupon-admin-service_coupon-delay-end_cg"
)
public class CouponDelayExecuteEndConsumer implements RocketMQListener<MessageWrapper<CouponExecuteStatusEvent>> {

    private final CouponMapper couponMapper;

    @Override
    public void onMessage(MessageWrapper<CouponExecuteStatusEvent> messageWrapper) {

        log.info("[消费者] 优惠卷结束定时执行, 消息体:{}", messageWrapper);
        CouponExecuteStatusEvent event = messageWrapper.getMessage();

        Coupon coupon = couponMapper.selectById(event.getCouponId());
        if (coupon == null) {
            return;
        }

//  如果当前时间 < 数据库中的 endTime
//    - 筛掉延后操作后的旧消息
        if (LocalDateTime.now().isBefore(coupon.getEndTime())) {
            log.info("忽略过期消息，couponId={}, msgDeliverTime={}",
                    coupon.getId(), event.getDeliverTime());
            return;
        }

        // 筛掉了提前操作后的旧消息
        LambdaUpdateWrapper<Coupon> wrapper = Wrappers.lambdaUpdate(Coupon.class)
                .eq(Coupon::getId, coupon.getId())
                .eq(Coupon::getStatus, CouponStatusEnum.AVAILABLE.getCode())
                .set(Coupon::getStatus, CouponStatusEnum.UNAVAILABLE.getCode());

        couponMapper.update(null, wrapper);

    }

}


