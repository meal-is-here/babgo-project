package com.babgo.controller.oreder;

import com.babgo.application.order.OrderInfo;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderResponse {
    private final UUID orderId;
    private final Long totalPrice;
    private final LocalDateTime cancelTime;
    private final String orderStatus;

    public static OrderResponse from(OrderInfo.CreateResult output) {
        return new OrderResponse(
                output.getOrderId(),
                output.getTotalPrice(),
                output.getCancelUntil(),
                output.getStatus()
        );
    }
}
