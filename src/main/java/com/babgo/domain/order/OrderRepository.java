package com.babgo.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    UUID findNextOrderId();
    Order save(Order orderModel);
    Page<Order> findOrders(UUID user, OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderId(UUID orderId);
}
