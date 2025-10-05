package com.babgo.domain.order;

import java.util.UUID;

public interface OrderRepository {
    UUID findNextOrderId();
    Order save(Order orderModel);
}
