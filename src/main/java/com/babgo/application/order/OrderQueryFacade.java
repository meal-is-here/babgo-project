package com.babgo.application.order;

import com.babgo.application.order.mapper.OrderMapper;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuService;
import com.babgo.domain.order.*;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueryFacade {

    private final OrderService orderService;
    private final StoreService storeService;
    private final MenuService menuService;

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

        List<UUID> menuIds = orderItems.stream()
                .map(OrderItem::getMenuId)
                .toList();

        List<Menu> menus = menuService.findAllByIds(menuIds);
        Map<UUID, Menu> menuMap = menus.stream()
                .collect(Collectors.toMap(Menu::getMenuId, menu -> menu));

        List<OrderInfo.Item> items = orderItems.stream()
                .map(orderItem -> {
                    Menu menu = menuMap.get(orderItem.getMenuId());
                    return OrderInfo.Item.from(orderItem, menu);
                })
                .toList();

        return OrderInfo.OrderAndItems.from(order, items);

    }
}
