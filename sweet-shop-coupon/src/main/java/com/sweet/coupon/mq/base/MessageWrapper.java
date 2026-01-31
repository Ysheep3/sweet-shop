package com.sweet.coupon.mq.base;

import lombok.*;

import java.io.Serializable;

/**
 * 封装消息体
 *
 * @param <T>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public final class MessageWrapper<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息发送 Keys
     */
    @NonNull
    private String keys;

    /**
     * 消息体
     */
    @NonNull
    private T message;

    /**
     * 推送时间
     */
    private long timestamp = System.currentTimeMillis();
}
