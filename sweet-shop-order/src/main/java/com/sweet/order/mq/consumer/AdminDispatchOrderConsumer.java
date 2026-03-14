package com.sweet.order.mq.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.sweet.common.constant.MessageConstant;
import com.sweet.common.exception.OrderBusinessException;
import com.sweet.order.common.OrderStatusEnum;
import com.sweet.order.entity.pojo.Order;
import com.sweet.order.mapper.OrderMapper;
import com.sweet.order.mq.base.MessageWrapper;
import com.sweet.order.mq.event.AdminDispatchOrderEvent;
import com.sweet.order.mq.producer.AdminDispatchOrderProducer;
import com.sweet.order.websocket.RiderWebSocketServer;
import com.sweet.order.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 消息消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j(topic = "AdminDispatchOrderConsumer")
@RocketMQMessageListener(
        topic = "sweet-shop-order-admin-service_order-delay_dispatch_topic",
        consumerGroup = "sweet-shop-order-admin-service_order-delay_dispatch_cg"
)
public class AdminDispatchOrderConsumer implements RocketMQListener<MessageWrapper<AdminDispatchOrderEvent>> {

    private final OrderMapper orderMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RiderWebSocketServer riderWebSocketServer;
    private final AdminDispatchOrderProducer adminDispatchOrderProducer;
    private final WebSocketServer webSocketServer;

    @Override
    public void onMessage(MessageWrapper<AdminDispatchOrderEvent> messageWrapper) {

        log.info("[消费者] 派单定时执行, 消息体:{}", messageWrapper);
        AdminDispatchOrderEvent event = messageWrapper.getMessage();

        Long orderId = event.getOrderId();
        String redisKey = event.getRedisKey();

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDERS_IS_NULL);
        }

        if (order.getDeliveryEmployeeId() != null) {
            // 第一个骑手接单了
            stringRedisTemplate.delete(redisKey);
            return;
        }

        String json = stringRedisTemplate.opsForValue().get(redisKey);

        if (json == null) {
            return;
        }

        List<Long> riders = JSON.parseArray(json, Long.class);

        // 第一个骑手没接单
        riders.remove(0);

        if (riders.isEmpty()) {
            // 所有人都没接单
            Map<String, Object> map = new HashMap<>();
            map.put("type", 3);
            map.put("orderId", order.getId().toString());
            map.put("content", "订单号:" + order.getOrderNo());

            webSocketServer.sendToAllClient(JSON.toJSONString(map));

            // 可以选择删除 redis key
            stringRedisTemplate.delete(redisKey);

            return;
        }

        stringRedisTemplate.opsForValue().set(
                redisKey,
                JSON.toJSONString(riders),
                2,
                TimeUnit.MINUTES
        );

        // 派给下一个
        Long nextRider = riders.get(0);

        Map<String, Object> map = BeanUtil.beanToMap(order);
        String message = JSON.toJSONString(map);

        riderWebSocketServer.sendToRider(nextRider, message);

        AdminDispatchOrderEvent adminDispatchOrderEvent = AdminDispatchOrderEvent.builder()
                .orderId(order.getId())
                .redisKey(redisKey)
                .deliverTime(System.currentTimeMillis() + 30000)
                .build();

        adminDispatchOrderProducer.sendMessage(adminDispatchOrderEvent);
    }

}