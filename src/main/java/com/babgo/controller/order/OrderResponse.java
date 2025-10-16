package com.babgo.controller.order;

import com.babgo.application.order.OrderInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderResponse {

    @Getter
    @RequiredArgsConstructor
    public static class Orders{
        private final List<OrderView> content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean hasNext;

        public static Orders from(OrderInfo.Orders output) {
            List<OrderView> summaries = output.getContent().stream()
                    .map(OrderView::from)
                    .toList();

            return new Orders(
                    summaries,
                    output.getPage(),
                    output.getSize(),
                    output.getTotalElements(),
                    output.getTotalPages(),
                    output.isHasNext()
            );
    }
    }

        @Getter
        @RequiredArgsConstructor
        public static class OrderView {
            private final String orderId;
            private final String storeName;
            private final Long totalPrice;
            private final String status;
            private final LocalDateTime createdAt;

            public static OrderView from(OrderInfo.OrderDetail detail) {
                return new OrderView(
                        detail.getOrderId(),
                        detail.getStoreName(),
                        detail.getTotalPrice(),
                        detail.getStatus(),
                        detail.getCreatedAt()
                );
            }
        }

    @Getter
    @RequiredArgsConstructor
    public static class Create {

        private final boolean ok;
        private final String message;
        private final UUID orderId;
        private final String orderStatus;
        private final Long totalPrice;
        private final List<InvalidItem> invalidItems;

        /**
         * 성공 결과 변환
         */
        public static Create from(OrderInfo.CreateResult output) {
            List<InvalidItem> invalids = Optional.ofNullable(output.getInvalidItems())
                    .orElse(List.of())
                    .stream()
                    .map(InvalidItem::from)
                    .toList();

            return new Create(
                    output.isOk(),
                    output.getMessage(),
                    output.getOrderId(),
                    output.getStatus(),
                    output.getTotalPrice(),
                    invalids
            );
        }

        /**
         * 실패(단순 메시지용) 결과 변환
         */
        public static Create fail(String message) {
            return new Create(false, message, null, null, null, List.of());
        }

        /**
         * 실패(상세 invalidItems 포함) 결과 변환
         */
        public static Create fail(String message, List<InvalidItem> invalidItems) {
            return new Create(false, message, null, null, null, invalidItems);
        }

        @Getter
        @RequiredArgsConstructor
        public static class InvalidItem {
            private final UUID menuId;
            private final String reason;

            public static InvalidItem from(OrderInfo.InvalidItem src) {
                return new InvalidItem(src.getMenuId(), src.getReason());
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class OrderDetail {

        private final UUID orderId;
        private final Long totalPrice;
        private final String deliveryAddress;
        private final String deliveryRequest;
        private final LocalDateTime createdAt;
        private final List<ItemView> items;

        @Getter
        @RequiredArgsConstructor
        public static class ItemView {
            private final UUID menuId;
            private final String menuName;
            private final Long price;
            private final Long totalPrice;
            private final Integer quantity;

            public static ItemView from(OrderInfo.Item item) {
                return new ItemView(
                        item.getMenuId(),
                        item.getMenuName(),
                        item.getPrice(),
                        item.getLineTotal(),
                        item.getQuantity()
                );
            }
        }

        public static OrderDetail from(OrderInfo.OrderAndItems output) {
            List<ItemView> itemDetails = output.getItems().stream()
                    .map(ItemView::from)
                    .toList();

            return new OrderDetail(
                    output.getOrderId(),
                    output.getTotalPrice(),
                    output.getDeliveryAddress(),
                    output.getDeliveryRequest(),
                    output.getCreatedAt(),
                    itemDetails
            );
        }
    }

    public static class Cancel {

    }
}
