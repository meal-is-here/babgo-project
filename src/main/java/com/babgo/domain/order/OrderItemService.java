package com.babgo.domain.order;

import com.babgo.application.order.OrderInfo;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuRepository;
import com.babgo.domain.menu.MenuStatus;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;

    public void create(List<OrderItemSnapshot> items, Order order) {
        List<OrderItem> item = items.stream().map(i ->
                OrderItem.of(
                        order,
                        i .getMenuId(),
                        i .getUnitPrice(),
                        i.lineTotal(),
                        i.getQuantity()
                )).toList();

        orderItemRepository.saveAll(item);
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
            backoff = @Backoff(delay = 120, multiplier = 2.0)
    )
    @Transactional
    public List<OrderItemSnapshot> reserveStockAndCreateOrderItems(
            UUID orderId,
            List<OrderInfo.OrderItemDetail> requestedItems
    ) {

        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "주문 항목이 비어 있습니다.");
        }

        Map<UUID, Integer> requestedQtyByMenuId = requestedItems.stream()
                .collect(Collectors.toMap(
                        OrderInfo.OrderItemDetail::getMenuId,
                        OrderInfo.OrderItemDetail::getQuantity,
                        Integer::sum
                ));


        List<UUID> targetMenuIds = new ArrayList<>(requestedQtyByMenuId.keySet());
        List<Menu> loadedMenus = menuRepository.findAllById(targetMenuIds);

        if (loadedMenus.size() != requestedQtyByMenuId.size()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "요청한 일부 메뉴를 찾을 수 없습니다.");
        }

        Map<UUID, Menu> menusById = loadedMenus.stream()
                .collect(Collectors.toMap(Menu::getMenuId, it -> it));


        for (Map.Entry<UUID, Integer> entry : requestedQtyByMenuId.entrySet()) {
            UUID menuId = entry.getKey();
            int qtyRequested = entry.getValue();

            if (qtyRequested <= 0) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "수량은 1 이상이어야 합니다. menuId=" + menuId);
            }

            Menu menu = menusById.get(menuId);
            if (menu.getMenuStatus() != MenuStatus.AVAILABLE) {
                throw new CustomException(ErrorCode.MENU_UNAVAILABLE, "판매 불가 메뉴가 포함되어 있습니다. menuId=" + menuId);
            }

            if (menu.getStock() < qtyRequested) {
                throw new CustomException(ErrorCode.OUT_OF_STOCK, "재고 부족: menuId=" + menuId);
            }
        }

        requestedQtyByMenuId.forEach((menuId, qtyRequested) -> {
            Menu menu = menusById.get(menuId);
            menu.decreaseStock(qtyRequested);
        });


        menuRepository.saveAll(menusById.values());

        return requestedQtyByMenuId.entrySet().stream()
                .map(e -> {
                    UUID menuId = e.getKey();
                    int qty = e.getValue();
                    Menu menu = menusById.get(menuId);
                    return OrderItemSnapshot.of(
                            menuId,
                            menu.getName(),
                            menu.getPrice(), // 서버 단가
                            qty
                    );
                })
                .toList();
    }

    @Recover
    public List<OrderItemSnapshot> recoverFromOptimisticLock(ObjectOptimisticLockingFailureException e,
                                                             UUID orderId,
                                                             List<OrderInfo.OrderItemDetail> requestedItems) {
        throw new CustomException(ErrorCode.CONFLICT, "요청이 증가하여 주문을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }

}

