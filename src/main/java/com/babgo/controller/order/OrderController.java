package com.babgo.controller.order;

import com.babgo.application.order.OrderFacade;
import com.babgo.application.order.OrderInfo;
import com.babgo.application.order.OrderQueryFacade;
import com.babgo.domain.user.User;
import com.babgo.global.api.ApiResponse;
import com.babgo.global.security.annotation.CurrentUser;
import com.babgo.global.security.annotation.RequireCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;
    private final OrderQueryFacade orderQueryFacade;

    @RequireCustomer
    @PostMapping()
    public ApiResponse<OrderResponse.Create> createOrder(
            @CurrentUser User user,
            @RequestBody OrderRequest.CreateOrder request
    ){
        OrderInfo.Create input = OrderInfo.Create.from(request);
        OrderInfo.CreateResult output = orderFacade.createOrder(user,input);
        OrderResponse.Create response = OrderResponse.Create.from(output);

        if (!output.isOk()) {
            return  ApiResponse.success(response.getMessage(), response);
        }
        return ApiResponse.success("주문이 생성되었습니다. 결제 정보를 입력해주세요.",response);
    }

    @RequireCustomer
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse.Cancel> cancelOrder(
            @CurrentUser User user,
            @PathVariable("orderId") UUID orderId
    ){
       OrderInfo.CancelResult result = orderFacade.cancelOrder(user.getUserId(), orderId);

        if (!result.isOk()) {
            return ApiResponse.success(result.getMessage());
        }

        return ApiResponse.success(result.getMessage());
    }

    @RequireCustomer
    @GetMapping()
    public ApiResponse<OrderResponse.Orders> getAllOrders(
            @CurrentUser User user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "LATEST") String sortType
    ){
        OrderInfo.Orders output = orderQueryFacade.getAllOrders(user.getUserId(), status, page, size, sortType);
        OrderResponse.Orders response = OrderResponse.Orders.from(output);
        return ApiResponse.success(response);
    }

    @RequireCustomer
    @GetMapping("{orderId}")
    public ApiResponse<OrderResponse.OrderDetail> getOrder(
            @CurrentUser User user,
            @PathVariable("orderId") UUID orderId
    ){
        OrderInfo.OrderAndItems output = orderQueryFacade.getOrderAndItems(user.getUserId(),orderId);
        OrderResponse.OrderDetail response = OrderResponse.OrderDetail.from(output);
        return ApiResponse.success(response);
    }
}
