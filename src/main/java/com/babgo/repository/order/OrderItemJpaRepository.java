package com.babgo.repository.order;

import com.babgo.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findAllByOrder_OrderId(UUID orderId);
}
