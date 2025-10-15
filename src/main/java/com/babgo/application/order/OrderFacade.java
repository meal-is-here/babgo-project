package com.babgo.application.order;

import com.babgo.domain.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

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

        //8. 응답 DTO
        //@하드코딩 대신 @Value/설정(예: order.cancel-window-seconds) 주입 권장. 또한 Clock 주입으로 테스트 용이성↑.
        LocalDateTime cancelTime = pendingOrder.getCreatedAt().plusSeconds(5);

        return OrderInfo.CreateResult.from(pendingOrder, cancelTime);
    }

    public void cancelOrder(UUID orderId, Long userId){
        Order order = orderService.getOrder(orderId);

        // 시간 측정 여기에서 바로 불가능 상태면 반환
        // 가능 상태이면 Order 및 payment 상태 파악 -> 분기

    }

}
