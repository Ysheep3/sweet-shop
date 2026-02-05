package com.sweet.coupon.mq.producer;

import com.sweet.coupon.mq.base.BaseSendExtendDTO;
import com.sweet.coupon.mq.base.MessageWrapper;
import com.sweet.coupon.mq.event.UserCouponRedeemEvent;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class UserCouponRedeemProducer extends AbstractCommonSendProduceTemplate<UserCouponRedeemEvent>{
    private ConfigurableEnvironment environment;
    public UserCouponRedeemProducer(
            @Autowired RocketMQTemplate rocketMQTemplate,
            @Autowired ConfigurableEnvironment configurableEnvironment) {
        super(rocketMQTemplate);
        this.environment = configurableEnvironment;
    }

    @Override
    protected BaseSendExtendDTO buildSendExtendDTO(UserCouponRedeemEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("用户兑换优惠卷")
                .topic(environment.resolvePlaceholders("sweet-shop-coupon_user-coupon-redeem-service_topic"))
                .keys(UUID.randomUUID().toString())
                .sentTimeout(1000L)
                .build();
    }

    @Override
    protected Message<?> buildMessage(UserCouponRedeemEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = Objects.nonNull(requestParam.getKeys()) ? requestParam.getKeys() : UUID.randomUUID().toString();
        return MessageBuilder
                .withPayload(new MessageWrapper<>(requestParam.getKeys(), messageSendEvent))
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .setHeader(MessageConst.PROPERTY_TAGS, requestParam.getTag())
                .build();
    }
}
