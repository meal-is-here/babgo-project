package com.babgo.domain.order;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public UUID createOrderId() {
        return orderRepository.findNextOrderId();
    }

    public Order create(Order order) {
        return orderRepository.save(order);
    }

    public Page<Order> findOrders(Long userId, OrderStatus status,Pageable pageable) {
        return orderRepository.findOrders(userId, status,pageable);
    }

    public List<OrderItem> findAllOrderItem(UUID orderId) {
        return orderItemRepository.orderItemRepository(orderId);
    }

    public Order getOrder(UUID orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,""));
    }

    @Transactional
    public void updateConfirmed(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,""));
        order.markConfirmed();
    }

    @Transactional
    public void markPaymentInProgress(UUID orderId) {

    }

    @Transactional
    public void updateCancel(Order order) {
        order.markCancel();
    }

    @Transactional
    public void updateCancelRequested(Order order) {
    }

    @Transactional
    public void updateRefundRequested(Order order) {
    }
}
