package com.babgo.repository.order;

import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderRepository;
import com.babgo.domain.order.OrderStatus;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public UUID findNextOrderId() {
        return UuidCreator.getTimeOrdered();
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Page<Order> findOrders(UUID userId, OrderStatus status, Pageable pageable) {
        return orderJpaRepository.findAllByUserIdAndOrderStatus(userId, status, pageable);
    }

}
