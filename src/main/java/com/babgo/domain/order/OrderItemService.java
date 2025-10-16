package com.babgo.domain.order;

import com.babgo.application.order.OrderInfo;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuRepository;
import com.babgo.domain.menu.MenuStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;

    public List<OrderItem> create(List<OrderItemSnapshot> items, Order order) {
        List<OrderItem> item = items.stream().map(i ->
                OrderItem.of(
                        order,
                        i .getMenuId(),
                        i .getUnitPrice(),
                        i.lineTotal(),
                        i.getQuantity()
                )).toList();

        return orderItemRepository.saveAll(item);
    }


    /**
     * 오더 아이템 검증:
     *  - 수량 유효성(>=1)
     *  - 같은 메뉴 중복 합산
     *  - 해당 가게 소속 여부
     *  - 메뉴 상태(AVAILABLE 등)
     *  - 소프트 재고 체크(현재 보유 재고 >= 요청 수량)
     *  - 서버 기준 가격으로 합계 계산
     */
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 10, multiplier = 2)
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderItemValidationResult reserveStockAndCreateOrderItems(List<OrderInfo.OrderItemDetail> requestedItems) {

        if (requestedItems == null || requestedItems.isEmpty()) {
            return new OrderItemValidationResult(List.of(), List.of(new OrderInfo.InvalidItem(null, "EMPTY_ITEMS")));
        }

        Map<UUID, Integer> qtyByMenuId = requestedItems.stream()
                .collect(Collectors.toMap(
                        OrderInfo.OrderItemDetail::getMenuId,
                        OrderInfo.OrderItemDetail::getQuantity,
                        Integer::sum
                ));

        List<Menu> loaded = menuRepository.findAllById(new ArrayList<>(qtyByMenuId.keySet()));
        Map<UUID, Menu> byId = loaded.stream().collect(Collectors.toMap(Menu::getMenuId, it -> it));

        List<OrderInfo.InvalidItem> invalids = new ArrayList<>();
        List<OrderItemSnapshot> valids = new ArrayList<>();

        for (var e : qtyByMenuId.entrySet()) {
            UUID menuId = e.getKey();
            int qty = e.getValue();
            Menu menu = byId.get(menuId);

            if (menu == null) {
                invalids.add(new OrderInfo.InvalidItem(menuId, "NOT_FOUND"));
                continue;
            }

            if (qty <= 0) {
                invalids.add(new OrderInfo.InvalidItem(menuId, "BAD_QUANTITY"));
                continue;
            }
            if (menu.getMenuStatus() != MenuStatus.AVAILABLE) {
                invalids.add(new OrderInfo.InvalidItem(menuId, "UNAVAILABLE"));
                continue;
            }
            if (menu.getStock() < qty) {
                invalids.add(new OrderInfo.InvalidItem(menuId, "OUT_OF_STOCK"));
                continue;
            }

            menu.decreaseStock(qty);
            valids.add(OrderItemSnapshot.of(menuId, menu.getName(), menu.getPrice(), qty));
        }

        if (!invalids.isEmpty()) {
            return new OrderItemValidationResult(List.of(), invalids);
        }

        menuRepository.saveAll(byId.values());

        return new OrderItemValidationResult(valids, List.of());
    }
}

