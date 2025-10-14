package com.babgo.domain.order;

import com.babgo.application.order.OrderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public void create(List<OrderInfo.OrderItemDetail> items, Order order) {
        List<OrderItem> item = items.stream().map(i ->
                OrderItem.of(
                        order,
                        i .getMenuId(),
                        i .getMenuOptionId(),
                        i .getClientPrice(),
                        10000L,
                        i.getQuantity()
                )).toList();

        orderItemRepository.saveAll(item);
    }

  /*  public List<OrderItem> verifyOrderItemsAvailability(
            List<OrderInfo.OrderItemDetail> items, UUID orderId
    ) {

        return r;
    }
    */
}
