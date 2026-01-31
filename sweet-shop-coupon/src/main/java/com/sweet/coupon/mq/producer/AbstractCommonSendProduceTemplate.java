package com.sweet.coupon.mq.producer;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.sweet.coupon.mq.base.BaseSendExtendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.messaging.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

@RequiredArgsConstructor
@Slf4j(topic = "CommonSendProduceTemplate")
public abstract class AbstractCommonSendProduceTemplate<T> {
    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 构建发送信息topic、messageKey、延迟时间等基础信息， messageSendEvent推送消息的事件
     *
     * @return
     */
    protected abstract BaseSendExtendDTO buildSendExtendDTO(T messageSendEvent);

    /**
     * 构建消息体
     *
     * @return
     */
    protected abstract Message<?> buildMessage(T messageSendEvent, BaseSendExtendDTO requestParam);

    /**
     * 通用消息发送器
     *
     * @return
     */
    public SendResult sendMessage(T messageSendEvent) {
        // 构建发送消息所需要的参数
        BaseSendExtendDTO baseSendExtendDTO = buildSendExtendDTO(messageSendEvent);
        SendResult sendResult = null;

        try {
            StringBuilder destinationBuilder = StrUtil.builder().append(baseSendExtendDTO.getTopic());
            // 构建 topic 名字
            if (baseSendExtendDTO.getTag() != null) {
                destinationBuilder.append(":").append(baseSendExtendDTO.getTag());
            }

            // 判断推送类型是什么
            if (baseSendExtendDTO.getDelayTime() == null) {
                // 没有延迟时间，普通发送
                sendResult = rocketMQTemplate.syncSend(
                        destinationBuilder.toString(),
                        buildMessage(messageSendEvent, baseSendExtendDTO),
                        // 如果消息发送时间超过指定的超时时间，会抛出超时异常。
                        baseSendExtendDTO.getSentTimeout());
            } else {
                // 有延时时间，发延时消息
                sendResult = rocketMQTemplate.syncSendDeliverTimeMills(
                        destinationBuilder.toString(),
                        buildMessage(messageSendEvent, baseSendExtendDTO),
                        baseSendExtendDTO.getDelayTime()
                );
            }
            log.info("[生产者] {} - 发送结果：{}，消息ID：{}，消息Keys：{}", baseSendExtendDTO.getEventName(), sendResult.getSendStatus(), sendResult.getMsgId(), baseSendExtendDTO.getKeys());
        } catch (Throwable ex) {
            log.error("[生产者] {} - 消息发送失败，消息体：{}", baseSendExtendDTO.getEventName(), JSON.toJSONString(messageSendEvent), ex);
        }

        return sendResult;
    }
}
