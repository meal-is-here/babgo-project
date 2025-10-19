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
            @RequestBody OrderRequest.CreateOrder request
    ){
        OrderInfo.Create input = OrderInfo.Create.from(request);
        OrderInfo.CreateResult output = orderFacade.createOrder(input);
        OrderResponse.Create response = OrderResponse.Create.from(output);

        if (!output.isOk()) {
            return  ApiResponse.success(response.getMessage(), response);
        }
        return ApiResponse.success("주문이 생성되었습니다. 결제 정보를 입력해주세요.",response);
    }


    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse.Cancel> cancelOrder(
            @PathVariable("orderId") UUID orderId
    ){
       Long userId = 1L;
       OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId);

        if (!result.isOk()) {
            return ApiResponse.success(result.getMessage());
        }

        return ApiResponse.success(result.getMessage());
    }

    @GetMapping()
    public ApiResponse<OrderResponse.Orders> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") String sortType
    ){
        // TODO: 실제 구현에선 인증 사용자에서 userId 가져오기
        Long userId = 1L;
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
