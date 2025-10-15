package com.babgo.controller.order;

import com.babgo.application.order.OrderFacade;
import com.babgo.application.order.OrderInfo;
import com.babgo.application.order.OrderQueryFacade;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;
    private final OrderQueryFacade orderQueryFacade;

    @PostMapping()
    public ApiResponse<OrderResponse.Create> createOrder(
          /*  @RequestHeader String idempotencyKey,*/
            @RequestBody OrderRequest.CreateOrder request
    ){
        String idempotencyKey ="dfsdfsd";
        OrderInfo.Create input = OrderInfo.Create.from(request);
        OrderInfo.CreateResult output = orderFacade.createOrder(idempotencyKey, input);
        OrderResponse.Create response = OrderResponse.Create.from(output);
        return ApiResponse.success("주문이 생성되었습니다. 결제 정보를 입력해주세요.",response);
    }

    /**
     * 의사 코드
     * 사용자가 주문 생성 후 5초 이내에 취소 요청을 보낼 수 있다.
     * 1. 5초 이내를 어떻게 검증할 것 인가.
     * 2. if 5초 이내 라면
     * ->payment 상태 -> READY -> 상태를 바꾸고 -> paymentCancel 실행
     * -> payment 상태 -> Processing ->
     * -> 또는 이미 완료 되었다면 -> 자동으로 환불로 전이
     *else 5초가 지나면 -> 사용자에게 취소가 불가능한 상태 -> 가게 또는 연락후 환불을 진행해주세요 알림
     */
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse.Cancel> cancelOrder(
            @PathVariable("orderId") UUID orderId
    ){
       Long userId = 1L;
       OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId, userId);

        if (!result.isOk()) {
            return ApiResponse.success(result.getMessage());
        }

        return ApiResponse.success(result.getMessage(), null);
    }

    @GetMapping()
    public ApiResponse<OrderResponse.Orders> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") String sortType
    ){
        // TODO: 실제 구현에선 인증 사용자에서 userId 가져오기
        UUID userId = UUID.fromString("11111111-2222-3333-4444-555555555555");
        OrderInfo.Orders output = orderQueryFacade.getAllOrders(userId, status, page, size, sortType);
        OrderResponse.Orders response = OrderResponse.Orders.from(output);
        return ApiResponse.success(response);
    }

    @GetMapping("{orderId}")
    public ApiResponse<OrderResponse.OrderDetail> getOrder(
            @PathVariable("orderId") UUID orderId
    ){
        OrderInfo.OrderAndItems output = orderQueryFacade.getOrderAndItems(orderId);
        OrderResponse.OrderDetail response = OrderResponse.OrderDetail.from(output);
        return ApiResponse.success(response);
    }
}
