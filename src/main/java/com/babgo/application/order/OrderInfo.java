package com.babgo.application.order;

import com.babgo.controller.order.OrderRequest;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderItem;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.store.Store;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class OrderInfo {

    @Getter
    @RequiredArgsConstructor
    public static class Create{
        private final UUID storeId;
        private final Long userId;
        private final String deliveryRequest;
        private final String deliveryAddress;
        private final List<OrderItemDetail> items;

        public static Create from(OrderRequest.CreateOrder request){
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
        private final UUID menuId;
        private final UUID menuOptionId;
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
    public static class OrderDetail {
        private final String orderId;
        private final String storeName;
        private final Long totalPrice;
        private final String status;
        private final LocalDateTime createdAt;

        public static OrderDetail from(Order order, Store store) {
            return new OrderDetail(
                    order.getOrderId().toString(),
                    store.getStoreName(),
                    order.getTotalPrice(),
                    order.getOrderStatus().getDescription(),
                    order.getCreatedAt()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CreateResult {

        private final boolean ok;
        private final String message;

        private final UUID orderId;
        private final String status;
        private final Long totalPrice;
        private final List<Item> items;

        private final List<InvalidItem> invalidItems;

        public static CreateResult ok(Order order,List<Item> items ) {
            return new CreateResult(
                    true,
                    "주문이 정상적으로 생성되었습니다.",
                    order.getOrderId(),
                    order.getOrderStatus().name(),
                    order.getTotalPrice(),
                    items,
                    List.of()
            );
        }

        public static CreateResult validated(List<Item> items) {
            long total = items.stream().mapToLong(Item::getLineTotal).sum();
            return new CreateResult(
                    true,
                    "검증이 완료되었습니다.",
                    null,
                    null,
                    total,
                    items,
                    List.of()
            );
        }

        public static CreateResult reject(String message) {
            return new CreateResult(
                    false,
                    message,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of()
            );
        }

        public static CreateResult reject(String message, List<InvalidItem> invalidItems) {
            return new CreateResult(
                    false,
                    message,
                    null,
                    null,
                    null,
                    List.of(),
                    invalidItems == null ? List.of() : invalidItems
            );
        }
    }
        @Getter
        @RequiredArgsConstructor
        public static class InvalidItem {
            private final UUID menuId;
            private final String reason;
        }

    @Getter
    @RequiredArgsConstructor
    public static class Orders {
        private final List<OrderDetail> content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean hasNext;

        public static Orders from(Page<OrderDetail> page) {
            return new Orders(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.hasNext()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class OrderAndItems {
        private final UUID orderId;
        private final long totalPrice;
        private final OrderStatus orderStatus;
        private final String deliveryAddress;
        private final String deliveryRequest;
        private final LocalDateTime createdAt;
        private final List<Item> items;
        //private final Payment payment;

        public static OrderAndItems from(Order order, List<Item> items){
            return new OrderAndItems(
                    order.getOrderId(),
                    order.getTotalPrice(),
                    order.getOrderStatus(),
                    order.getDeliveryAddress(),
                    order.getDeliveryRequest(),
                    order.getCreatedAt(),
                    items
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Item {
        private final UUID orderItemId;
        private final UUID menuId;
        private final String menuName;
        private final Long price;
        private final int quantity;
        private final Long lineTotal;

        public static Item from(OrderItem item, Menu menu){
            return new Item(
                    item.getOrderItemId(),
                    menu.getMenuId(),
                    menu.getName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getTotalPrice()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CancelResult {

        private final boolean ok;
        private final String message;

        public static CancelResult ok(String message) {
            return new CancelResult(true, message);
        }

        public static CancelResult reject(String message) {
            return new CancelResult(false, message);
        }
    }

}
