package com.babgo.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    UUID findNextOrderId();
    Order save(Order orderModel);
    Page<Order> findOrders(Long user, OrderStatus status, Pageable pageable);
    Optional<Order> findByOrderId(UUID orderId);
    boolean existsByOrderIdAndUserId(UUID orderId, Long userId);
    Optional<Order> findByOrderIdAndUserId(UUID orderId, Long userId);
}
