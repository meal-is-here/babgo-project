package com.babgo.application.order;

import com.babgo.controller.oreder.OrderRequest;
import com.babgo.domain.order.Order;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class OrderInfo {

    /**
     * input
     */
    @Getter
    @RequiredArgsConstructor
    public static class Create{
        private final String storeId;
        private final String userId;
        private final String deliveryRequest;
        private final String deliveryAddress;
        private final List<OrderItemDetail> items;

        public static Create from(OrderRequest.createOrder request){
            List<OrderItemDetail> items =
                    List.copyOf(
                            request.getItems().stream()
                                    .map(OrderItemDetail::from)
                                    .toList()
                    );
            return new Create(
                    request.getStoreId(),
                    request.getUserId(),
                    request.getDeliveryRequest(),
                    request.getDeliveryAddress(),
                    items
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class OrderItemDetail {
        private final String menuId;
        private final String menuOptionId;
        private final Long clientPrice;
        private final Integer quantity;

        public static OrderItemDetail from(OrderRequest.OrderItemRequest requestItem){
            return new OrderItemDetail(
                    requestItem.getMenuId(),
                    requestItem.getMenuOptionId(),
                    requestItem.getPrice(),
                    requestItem.getQuantity()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CreateResult {
        private final UUID orderId;
        private final String status;
        private final Long totalPrice;
        private final LocalDateTime cancelUntil;

        public static CreateResult from(Order order, LocalDateTime cancelUntil) {
            return new CreateResult(
                    order.getOrderId(),
                    order.getOrderStatus().name(),
                    order.getTotalPrice(),
                    cancelUntil
            );
        }
    }



}
