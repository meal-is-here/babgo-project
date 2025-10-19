package com.babgo.repository.store;

import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.store.StoreNotificationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreNotificationAdapter implements StoreNotificationPort {

    private final StoreSseHub sseHub;

    @Override
    public void notifyOrderStatusChanged(Long userId, UUID orderId, OrderStatus status, String message) {
        Map<String, Object> payload = Map.of(
                "type", "ORDER_STATUS_CHANGED",
                "orderId", orderId.toString(),
                "status", status.name(),
                "message", message
        );
        sseHub.notify(userId, "ORDER_STATUS_CHANGED", payload);
    }
}
