package com.babgo.controller.oreder;

import com.babgo.application.order.OrderFacade;
import com.babgo.application.order.OrderInfo;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping()
    public ApiResponse<OrderResponse> createOrder(
            @RequestHeader String idempotencyKey,
            @RequestBody OrderRequest.createOrder request
    ){
        OrderInfo.Create input = OrderInfo.Create.from(request);
        OrderInfo.CreateResult output = orderFacade.createOrder(idempotencyKey, input);
        OrderResponse response = OrderResponse.from(output);
        return ApiResponse.success("주문이 접수되었습니다. 취소는 5초 이내 가능합니다.",response);
    }
}
