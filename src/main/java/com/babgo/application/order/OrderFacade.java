package com.babgo.application.order;

import com.babgo.application.order.event.OrderCreatedEvent;
import com.babgo.application.order.port.CancelWindow;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuService;
import com.babgo.domain.order.*;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final StoreService storeService;
    private final MenuService menuService;
    private final ApplicationEventPublisher eventPublisher;
    private final CancelWindow cancelWindow;

    @Transactional
    public OrderInfo.CreateResult createOrder( OrderInfo.Create input){
        //1. 사용자 검증
        //User user = "userService.getUser(info.userId)";
        Long user = 1L;
        //2.idempotencyKey 검증
        // checkIdempotency(idempotencyKey);

        //3. !checkIdempotency 아이디 생성
        UUID orderId = orderService.createOrderId();

        // 가게 존재하고 오픈 상태, 주문 가능인지 확인
        Store store = storeService.findByStoreId(input.getStoreId());
        if (!store.isOrderable(LocalTime.now())) {
            return OrderInfo.CreateResult.reject("현재 가게 운영 시간이 아닙니다.");
        }
        //4. 요청 아이템 → 엔티티 변환 오더 아이템 재고 있는지 검증 후 검증된 객체 리스트 반환
        OrderItemValidationResult validation = orderItemService.reserveStockAndCreateOrderItems(input.getItems());
        if (validation.hasInvalid()) return OrderInfo.CreateResult.reject("일부 메뉴가 주문 불가합니다.", validation.getInvalidItems());

        //5. 총액 계산(서버 기준)
        List<OrderItemSnapshot> orderItemsSnapshot = validation.getValidItems();
        long totalPrice = orderItemsSnapshot.stream()
                .mapToLong(OrderItemSnapshot::lineTotal)
                .sum();

        // 6) 주문 엔티티 생성/저장
        Order order = Order.of(
                orderId,
                store.getStoreId(),
                user,
                input.getDeliveryRequest(),
                input.getDeliveryAddress(),
                totalPrice
        );

        Order pendingOrder = orderService.create(order);

        //7. 검증 완료된 오더 아이템 저장
        List<OrderItem> orderItems = orderItemService.create(orderItemsSnapshot, pendingOrder);
        
        // 메뉴 정보 조회 및 결합
        List<UUID> menuIds = orderItems.stream()
                .map(OrderItem::getMenuId)
                .toList();
        List<Menu> menus = menuService.findAllByIds(menuIds);
        Map<UUID, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getMenuId, menu -> menu));
        
        List<OrderInfo.Item> items = orderItems.stream()
                .map(orderItem -> {
                    Menu menu = menuMap.get(orderItem.getMenuId());
                    return OrderInfo.Item.from(orderItem, menu);
                })
                .toList();

        // 8) 이벤트 발행
        eventPublisher.publishEvent(new OrderCreatedEvent(orderId));

        // 9) 성공 결과
        return OrderInfo.CreateResult.ok(pendingOrder, items);
    }

    @Transactional
    public OrderInfo.CancelResult cancelOrder(UUID orderId){

        if (!cancelWindow.isOpen(orderId)) {
            return OrderInfo.CancelResult.reject("이미 취소되었거나, 취소 가능 시간이 만료되었습니다.");
        }

        Order order = orderService.getOrder(orderId);
        try {
            switch (order.getOrderStatus()) {
                case PENDING -> {
                    orderService.updateCancel(order);
                    List<OrderItem> items = orderService.findAllOrderItem(orderId);

                    for (OrderItem item : items) {
                        menuService.increaseStock(item.getMenuId(), item.getQuantity());
                    }

                    cancelWindow.close(orderId);
                    return OrderInfo.CancelResult.ok("주문을 취소했습니다.");
                }
                case PAYMENT_IN_PROGRESS -> {
                    // paymentService.requestCancel(order);
                    orderService.updateCancelRequested(order);
                    cancelWindow.close(orderId);
                    return OrderInfo.CancelResult.ok("결제 취소를 요청했습니다. 처리 중입니다.");
                }
                case CONFIRMED -> {
                    //refundService.requestRefund(order);
                    orderService.updateRefundRequested(order);
                    cancelWindow.close(orderId);
                    return OrderInfo.CancelResult.ok("환불을 요청했습니다. 처리 중입니다.");
                }
                case CANCELED, CANCEL_REQUESTED -> {
                    return OrderInfo.CancelResult.reject("이미 취소된 주문입니다.");
                }

                case REFUNDED, REFUND_REQUESTED -> {
                    return OrderInfo.CancelResult.reject("이미 환불 처리 경로에 있습니다.");
                }

                default -> {
                    return OrderInfo.CancelResult.reject("현재 상태에서는 취소할 수 없습니다.");
                }
            }
        }catch (OptimisticLockingFailureException | jakarta.persistence.OptimisticLockException e) {
            return OrderInfo.CancelResult.reject("이미 상태가 변경되었습니다." + order.getOrderStatus().getDescription());
        } catch (IllegalStateException e) {
            return OrderInfo.CancelResult.reject(e.getMessage());
        }
    }

}
