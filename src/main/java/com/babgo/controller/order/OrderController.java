package com.babgo.controller.order;

import com.babgo.application.order.OrderFacade;
import com.babgo.application.order.OrderInfo;
import com.babgo.application.order.OrderQueryFacade;
import com.babgo.global.api.ApiResponse;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;
    private final OrderQueryFacade orderQueryFacade;

    @PermitAll
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
