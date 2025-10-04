package com.babgo.repository.order;

import com.babgo.domain.order.OrderItem;
import com.babgo.domain.order.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public void saveAll(List<OrderItem> items) {
        orderItemJpaRepository.saveAll(items);
    }
}
