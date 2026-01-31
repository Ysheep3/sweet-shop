package com.sweet.coupon.mq.producer;

import com.sweet.coupon.mq.base.BaseSendExtendDTO;
import com.sweet.coupon.mq.base.MessageWrapper;
import com.sweet.coupon.mq.event.CouponExecuteStatusEvent;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * 延迟推送 结束优惠卷 消息
 */
@Component
public class CouponDelayExecuteStartProducer extends AbstractCommonSendProduceTemplate<CouponExecuteStatusEvent> {
    private final ConfigurableEnvironment configurableEnvironment;

    public CouponDelayExecuteStartProducer(
            @Autowired RocketMQTemplate rocketMQTemplate,
            @Autowired ConfigurableEnvironment configurableEnvironment) {

        super(rocketMQTemplate);
        this.configurableEnvironment = configurableEnvironment;
    }

    @Override
    protected BaseSendExtendDTO buildSendExtendDTO(CouponExecuteStatusEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("启用优惠卷任务")
                .keys(messageSendEvent.getCouponId().toString())
                .topic(configurableEnvironment.resolvePlaceholders("sweet-shop-coupon-admin-service_coupon-delay_start_topic"))
                .delayTime(messageSendEvent.getDeliverTime())
                .sentTimeout(10000L)
                .build();
    }

    @Override
    protected Message<?> buildMessage(CouponExecuteStatusEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = Objects.nonNull(requestParam.getKeys())
                ? requestParam.getKeys()
                : UUID.randomUUID().toString();

        return MessageBuilder
                //发送消息体，在消费者中接收
                .withPayload(new MessageWrapper<>(keys, messageSendEvent))
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .setHeader(MessageConst.PROPERTY_TAGS, requestParam.getTag())
                .build();
    }
}
