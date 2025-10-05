package com.babgo.domain.order;

import java.util.List;

public interface OrderItemRepository {
    void saveAll(List<OrderItem> items);
}
