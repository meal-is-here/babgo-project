package com.babgo.application.order;

import com.babgo.application.order.mapper.OrderMapper;
import com.babgo.domain.order.*;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ssl.PemSslBundleProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueryFacade {

    private final OrderService orderService;
    private final StoreService storeService;

    @Transactional(readOnly = true)
    public OrderInfo.Orders getAllOrders(
            Long userId,
            String status,
            int page,
            int size,
            String sortType
    ) {
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 10);

        Pageable pageable = PageRequest.of(safePage, safeSize, OrderMapper.toSort(sortType));
        OrderStatus orderStatus = OrderMapper.toStatus(status);

        Page<Order> orders = orderService.findOrders(userId,orderStatus, pageable);

        Page<OrderInfo.OrderDetail> orderDetails = orders.map(order -> {
            Store store = storeService.findByStoreId(order.getStoreId()); // Optional이면 orElseThrow/nullable 처리
            return OrderInfo.OrderDetail.from(order, store);
        });

        return OrderInfo.Orders.from(orderDetails);
    }

    @Transactional(readOnly = true)
    public OrderInfo.OrderAndItems getOrderAndItems(UUID orderId) {
        Order order = orderService.getOrder(orderId);
        List<OrderItem> orderItems = orderService.findAllOrderItem(order.getOrderId());

        List<OrderInfo.Item> items = orderItems
                .stream()
                .map(OrderInfo.Item::from)
                .toList();

        return OrderInfo.OrderAndItems.from(order,items);

    }
}
