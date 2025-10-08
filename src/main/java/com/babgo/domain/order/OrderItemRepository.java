package com.babgo.domain.order;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository {
    void saveAll(List<OrderItem> items);
    List<OrderItem> orderItemRepository(UUID orderId);
}
