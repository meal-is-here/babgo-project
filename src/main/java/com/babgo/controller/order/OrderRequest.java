package com.babgo.controller.order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class OrderRequest {

    @Getter
    @RequiredArgsConstructor
    public static class CreateOrder{
        private final String storeId;
        private final String userId;
        private final String deliveryRequest;
        private final String deliveryAddress;
        private final List<OrderItemRequest> items;

    }

    @Getter
    @RequiredArgsConstructor
    public static class OrderItemRequest {
        private final String menuId;
        private final String menuOptionId;
        private final Long price;
        private final Integer quantity;
    }
}
