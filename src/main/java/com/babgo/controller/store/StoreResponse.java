package com.babgo.controller.store;

import com.babgo.application.store.StoreInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreResponse {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OrderStatusResult {

        private final UUID orderId;
        private final String status;
        private final String statusDesc;

        public static OrderStatusResult from(StoreInfo.OrderStatusResult output) {
            return new OrderStatusResult(
                    output.getOrderId(),
                    output.getStatus().name(),
                    output.getStatus().getDescription()
            );
        }
    }
}
