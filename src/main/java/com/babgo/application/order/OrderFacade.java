package com.babgo.application.order;

import com.babgo.application.order.event.OrderCreatedEvent;
import com.babgo.domain.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ApplicationEventPublisher eventPublisher;
    private final CancelWindow cancelWindow;
    @Transactional
    public OrderInfo.CreateResult createOrder(String idempotencyKey, OrderInfo.Create input){
        //1. 사용자 검증
        //User user = "userService.getUser(info.userId)";
        Long user = 1L;
        //2.idempotencyKey 검증
        // checkIdempotency(idempotencyKey);

        //3. !checkIdempotency 아이디 생성
        UUID orderId = orderService.createOrderId();

        // 가게 존재하고 오픈 상태, 배달 다능 지역 확인
        // Store store = "storeService.getStore(info.storeId)";
        UUID store = UUID.randomUUID();

        //4. 요청 아이템 → 엔티티 변환 오더 아이템 재고 있는지 검증 후 검증된 객체 리스트 반환
       /* List<OrderItem> items = orderItemService.verifyOrderItemsAvailability(input.getItems(), orderId);
*/
        //5. 총액 계산(서버 기준)
      /*  Long totalPrice = orderService.calculateTotal(items);*/
        Long totalPrice =10000L;

        //오더 임시 객체 생성
        Order order = Order.of(
                orderId,
                store,
                user,
                input.getDeliveryRequest(),
                input.getDeliveryAddress(),
                totalPrice
        );

        //6. 저장 (Service 내부에서 order 먼저, 그 다음 items 저장)
        Order pendingOrder = orderService.create(order);

        //7. 검증 완료된 오더 아이템 저장
        orderItemService.create(input.getItems(), pendingOrder);

        eventPublisher.publishEvent(new OrderCreatedEvent(orderId));
        return OrderInfo.CreateResult.from(pendingOrder);
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
                    cancelWindow.close(orderId);
                    return OrderInfo.CancelResult.ok("주문을 취소했습니다.");
                }
                case PAYMENT_IN_PROGRESS -> {
                    // 결제 진행중: PG 취소 요청 비동기
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
