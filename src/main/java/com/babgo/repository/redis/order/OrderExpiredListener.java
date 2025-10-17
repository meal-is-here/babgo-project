package com.babgo.repository.redis.order;

import com.babgo.application.order.event.OrderExpiredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpiredListener implements MessageListener {

    private final ApplicationEventPublisher eventPublisher;
    private final OrderRedisProps props;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Redis key expired: {}", expiredKey);

        // 취소 윈도우 키만 처리
        if (!expiredKey.contains(props.cancelWindowSuffix())) {
            return;
        }

        try {
            String orderIdStr = extractOrderId(expiredKey);
            UUID orderId = UUID.fromString(orderIdStr);

            eventPublisher.publishEvent(new OrderExpiredEvent(orderId));

        } catch (Exception e) {
            log.error("Failed to process expired order key: {}", expiredKey, e);
        }
    }

    private String extractOrderId(String key) {
        String prefix = props.keyPrefix();
        String suffix = props.cancelWindowSuffix();

        return key
                .replace(prefix, "")
                .replace(suffix, "");
    }
}