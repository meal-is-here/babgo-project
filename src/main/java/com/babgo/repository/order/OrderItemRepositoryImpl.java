package com.babgo.repository.order;

import com.babgo.domain.order.OrderItem;
import com.babgo.domain.order.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public List<OrderItem>  saveAll(List<OrderItem> items) {
        return orderItemJpaRepository.saveAll(items);
    }

    @Override
    public List<OrderItem> orderItemRepository(UUID orderId) {
        return orderItemJpaRepository.findAllByOrder_OrderId(orderId);
    }
}
