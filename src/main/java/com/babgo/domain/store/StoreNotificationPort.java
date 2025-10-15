package com.babgo.domain.store;

import com.babgo.domain.order.OrderStatus;

import java.util.UUID;

public interface StoreNotificationPort {
    void notifyOrderStatusChanged(Long userId, UUID orderId, OrderStatus orderStatus, String message);
}
