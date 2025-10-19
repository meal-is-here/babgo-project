package com.babgo.domain.order;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

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

    public void validateOrderOwnership(UUID orderId, Long userId) {
        if (!orderRepository.existsByOrderIdAndUserId(orderId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "해당 주문에 대한 권한이 없습니다.");
        }
    }

    public Order getOrderWithAuth(UUID orderId, Long userId) {
        return orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN, "해당 주문에 대한 권한이 없습니다."));
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

    @Transactional
    public void updateFailed(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,""));
        order.markCancel();
    }

    @Transactional
    public void expireOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (order.getOrderStatus() == OrderStatus.PENDING) {
            order.markExpired();
        }
    }

}
