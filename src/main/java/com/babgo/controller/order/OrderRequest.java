package com.babgo.controller.order;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderRequest {

    @Getter
    @RequiredArgsConstructor
    public static class CreateOrder{
        private final String storeId;
        private final Long userId;
        private final String deliveryRequest;
        private final String deliveryAddress;
        private final List<OrderItemRequest> items;

    }

    @Getter
    @RequiredArgsConstructor
    public static class OrderItemRequest {
        private final UUID menuId;
        private final UUID menuOptionId;
        private final Long price;
        private final Integer quantity;
    }
}
