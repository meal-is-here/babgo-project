package com.babgo.application.order.event;

import com.babgo.domain.menu.MenuService;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderItem;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.repository.redis.order.OrderCancelRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderCancelRedisRepository paymentCancelRedisRepository;
    private final OrderService orderService;
    private final MenuService menuService;

    //오더 생성이 커밋이 완료 된 이후에 이벤트를 통해 레디스에 ttl 생성
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent e) {
        paymentCancelRedisRepository.open(e.orderId());
    }


    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderExpired(OrderExpiredEvent e) {
        log.info("주문 만료 처리 시작 - orderId: {}", e.orderId());

        try {
            Order order = orderService.getOrder(e.orderId());

            // PENDING 상태만 처리
            if (order.getOrderStatus() != OrderStatus.PENDING) {
                log.warn("이미 처리된 주문 - orderId: {}, status: {}",
                        e.orderId(), order.getOrderStatus());
                return;
            }

            // 1. 주문 상태를 EXPIRED로 변경
            orderService.expireOrder(e.orderId());

            // 2. 재고 복구 (기존 MenuService 재사용)
            List<OrderItem> items = orderService.findAllOrderItem(e.orderId());
            for (OrderItem item : items) {
                menuService.increaseStock(item.getMenuId(), item.getQuantity());
                log.info("재고 복구 완료 - menuId: {}, quantity: {}",
                        item.getMenuId(), item.getQuantity());
            }

            log.info("주문 만료 처리 완료 - orderId: {}", e.orderId());

        } catch (Exception ex) {
            log.error("주문 만료 처리 실패 - orderId: {}", e.orderId(), ex);
            throw ex;
        }
    }
}
