package com.babgo.controller.order;

import com.babgo.application.order.OrderInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderResponse {

    @Getter
    @RequiredArgsConstructor
    public static class Orders{
        private final List<OrderDetail> content;
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final boolean hasNext;

        public static Orders from(OrderInfo.Orders output) {
            List<OrderDetail> summaries = output.getContent().stream()
                    .map(OrderDetail::from)
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
        public static class OrderDetail {
            private final String orderId;
            private final String storeName;
            private final Long totalPrice;
            private final String status;
            private final LocalDateTime createdAt;

            public static OrderDetail from(OrderInfo.OrderDetail detail) {
                return new OrderDetail(
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
        private final UUID orderId;
        private final Long totalPrice;
        private final LocalDateTime cancelTime;
        private final String orderStatus;

        public static OrderResponse.Create from(OrderInfo.CreateResult output) {
            return new OrderResponse.Create(
                    output.getOrderId(),
                    output.getTotalPrice(),
                    output.getCancelUntil(),
                    output.getStatus()
            );
        }
    }
}
