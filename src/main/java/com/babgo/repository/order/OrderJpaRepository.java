package com.babgo.repository.order;

import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
    Page<Order> findAllByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus, Pageable pageable);

    boolean existsByOrderIdAndUserId(UUID orderId, Long userId);

    Optional<Order> findByOrderIdAndUserId(UUID orderId, Long userId);
}
