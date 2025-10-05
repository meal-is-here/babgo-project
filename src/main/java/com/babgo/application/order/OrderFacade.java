package com.babgo.application.order;

import com.babgo.domain.order.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @Transactional
    public OrderInfo.CreateResult createOrder(String idempotencyKey, OrderInfo.Create input){
        //1. 사용자 검증
        //User user = "userService.getUser(info.userId)";

        //2.idempotencyKey 검증
        // checkIdempotency(idempotencyKey);

        //3. !checkIdempotency 아이디 생성
        UUID orderId = orderService.createOrderId();

        // 가게 존재하고 오픈 상태, 배달 다능 지역 확인
        // Store store = "storeService.getStore(info.storeId)";

        //4. 요청 아이템 → 엔티티 변환 오더 아이템 재고 있는지 검증 후 검증된 객체 리스트 반환
        List<OrderItem> items = orderItemService.verifyOrderItemsAvailability(input.getItems(), orderId);

        //5. 총액 계산(서버 기준)
        Long totalPrice = orderService.calculateTotal(items);

        //오더 임시 객체 생성
        Order order = Order.of(
                orderId,
                "store1",
                "user1",
                input.getDeliveryRequest(),
                input.getDeliveryAddress(),
                totalPrice
        );

        //6. 저장 (Service 내부에서 order 먼저, 그 다음 items 저장)
        Order pendingOrder = orderService.create(order);

        //7. 검증 완료된 오더 아이템 저장
        orderItemService.create(items);

        //8. 응답 DTO
        LocalDateTime cancelTime = pendingOrder.getCreatedAt().plusSeconds(5);

        return OrderInfo.CreateResult.from(pendingOrder, cancelTime);
    }

}
