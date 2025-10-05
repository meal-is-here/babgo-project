package com.babgo.domain.order;

import com.babgo.application.order.OrderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public void create(List<OrderItem> items) {
        orderItemRepository.saveAll(items);
    }

    public List<OrderItem> verifyOrderItemsAvailability(
            List<OrderInfo.OrderItemDetail> items,
            UUID orderId
    ) {

        return null;
    }
}
