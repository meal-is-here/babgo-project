package com.babgo.application.order;

import com.babgo.application.order.mapper.OrderMapper;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderSortType;
import com.babgo.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class OrderQueryFacade {

    private final OrderService orderService;

    @Transactional(readOnly = true)
    public OrderInfo.Orders getAllOrders(
            UUID userId,
            String status,
            int page,
            int size,
            String sortType
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 10);
        Pageable pageable = PageRequest.of(safePage,safeSize, OrderMapper.toSort(sortType));
        OrderStatus orderStatus = OrderMapper.toStatus(status);

        Page<Order> orders = orderService.findOrders(userId, orderStatus, pageable);

        Page<OrderInfo.OrderDetail> orderDetails = orders.map(OrderInfo.OrderDetail::from);
        return  OrderInfo.Orders.from(orderDetails);
    }
}
