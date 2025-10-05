package com.babgo.repository.order;

import com.babgo.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {
}
