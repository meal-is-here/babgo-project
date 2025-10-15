package com.babgo.repository.payment;

import com.babgo.application.order.CancelWindow;
import com.babgo.repository.redis.order.OrderRedisProps;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class PaymentCancelRedisRepository implements CancelWindow {

    private final RedisTemplate<String, String> redis;
    private final OrderRedisProps props;

    public PaymentCancelRedisRepository(
            @Qualifier("orderRedisTemplate") RedisTemplate<String, String> redis,
            OrderRedisProps props
    ) {
        this.redis = redis;
        this.props = props;
    }

    private String cancelKey(UUID orderId) {
        return props.keyPrefix() + orderId + props.cancelWindowSuffix();
    }

    @Override
    public void open(UUID orderId) {
        redis.opsForValue().setIfAbsent(
                cancelKey(orderId), "1",
                props.cancelWindowTtlSeconds(), TimeUnit.SECONDS
        );
    }

    @Override
    public boolean isOpen(UUID orderId) {
        return redis.hasKey(cancelKey(orderId));
    }

    @Override
    public void close(UUID orderId) {
        redis.delete(cancelKey(orderId));
    }

    //5분 이내 결제가 이루어지지 않았을 경우 쓰레기 ,, 주문이라고 판단하고 주문 상태 변경
}
