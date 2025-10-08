package com.babgo.repository.order;

import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {


    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public UUID findNextOrderId() {
        return UuidCreator.getTimeOrdered();
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

}
