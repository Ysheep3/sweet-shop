package com.sweet.order.mq.producer;

import com.sweet.order.mq.base.BaseSendExtendDTO;
import com.sweet.order.mq.base.MessageWrapper;
import com.sweet.order.mq.event.AdminDispatchOrderEvent;
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
public class AdminDispatchOrderProducer extends AbstractCommonSendProduceTemplate<AdminDispatchOrderEvent> {
    private final ConfigurableEnvironment configurableEnvironment;

    public AdminDispatchOrderProducer(
            @Autowired RocketMQTemplate rocketMQTemplate,
            @Autowired ConfigurableEnvironment configurableEnvironment) {

        super(rocketMQTemplate);
        this.configurableEnvironment = configurableEnvironment;
    }

    @Override
    protected BaseSendExtendDTO buildSendExtendDTO(AdminDispatchOrderEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("派单定时执行任务")
                .keys(messageSendEvent.getOrderId().toString())
                .topic(configurableEnvironment.resolvePlaceholders("sweet-shop-order-admin-service_order-delay_dispatch_topic"))
                .delayTime(messageSendEvent.getDeliverTime())
                .sentTimeout(10000L)
                .build();
    }

    @Override
    protected Message<?> buildMessage(AdminDispatchOrderEvent messageSendEvent, BaseSendExtendDTO requestParam) {
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
