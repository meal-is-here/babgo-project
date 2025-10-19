package com.babgo.repository.redis.order;

import com.babgo.application.order.port.CancelWindow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderCancelRedisRepository implements CancelWindow {

    private final RedisTemplate<String, String> redis;
    private final OrderRedisProps props;

    public OrderCancelRedisRepository(
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
        String key = cancelKey(orderId);
        
        Boolean result = redis.opsForValue().setIfAbsent(
                key, "1",
                props.cancelWindowTtlSeconds(), TimeUnit.SECONDS
        );

        Boolean exists = redis.hasKey(key);
        Long ttl = redis.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public boolean isOpen(UUID orderId) {
        return redis.hasKey(cancelKey(orderId));
    }

    @Override
    public void close(UUID orderId) {
        redis.delete(cancelKey(orderId));
    }

}
