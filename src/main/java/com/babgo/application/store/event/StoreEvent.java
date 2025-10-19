package com.babgo.application.store.event;

import com.babgo.domain.order.OrderStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreEvent {

    public record StatusChanged(Long userId, UUID orderId, OrderStatus status, String message) {
        public static StatusChanged of(Long userId, UUID orderId, OrderStatus status, String message) {
            return new StatusChanged(userId, orderId, status, message);
        }
    }
}
