package com.babgo.application.store;


import com.babgo.application.store.event.StoreEvent;
import com.babgo.application.store.event.StoreOrderCompletedEvent;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import com.babgo.domain.user.User;
import com.babgo.domain.user.UserRole;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreFacade {

    private final StoreService storeService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createStore(User user, StoreInfo.Create input) {
        Category category = categoryService.findByCategoryId(input.getCategoryId());
        Long ownerId = user.getUserId();
        Store store = Store.of(
            ownerId,
            input.getStoreName(),
            input.getAddressLine(),
            input.getLatitude(),
            input.getLongitude(),
            input.getRegionCode(),
            input.getPhoneNumber(),
            input.getMinOrderAmount(),
            input.getOpeningHours(),
            input.getClosingHours(),
            category
        );
        storeService.create(store, displayName(user));
    }

    @Transactional
    public void updateStore(User user,UUID storeId, StoreInfo.Update input) {
        Store store = storeService.findByStoreId(storeId);
        verifyOwnerOrAdmin(user, store);
        Map<String, Object> changes = new HashMap<>();
        if (input.getStoreName() != null) changes.put("storeName", input.getStoreName());
        if (input.getAddressLine() != null) changes.put("addressLine", input.getAddressLine());
        if (input.getLatitude() != null) changes.put("latitude", input.getLatitude());
        if (input.getLongitude() != null) changes.put("longitude", input.getLongitude());
        if (input.getRegionCode() != null) changes.put("regionCode", input.getRegionCode());
        if (input.getPhoneNumber() != null) changes.put("phoneNumber", input.getPhoneNumber());
        if (input.getMinOrderAmount() != null) changes.put("minOrderAmount", input.getMinOrderAmount());
        if (input.getOpeningHours() != null) changes.put("openingHours", input.getOpeningHours());
        if (input.getClosingHours() != null) changes.put("closingHours", input.getClosingHours());
        if (input.getCategoryId() != null) changes.put("categoryId", input.getCategoryId());

        storeService.update(store, changes, displayName(user));
    }

    @Transactional
    public void deleteStore(User user,UUID storeId) {
        Store store = storeService.findByStoreId(storeId);
        verifyOwnerOrAdmin(user, store);
        storeService.delete(store, displayName(user));
    }

    // 가게조회
    public StoreInfo.Detail getStoreById(UUID id) {
        Store store = storeService.getStoreById(id)
            .orElseThrow(() -> new RuntimeException("STORE_NOT_FOUND"));
        return StoreInfo.Detail.fromEntity(store);
    }

    // 가게요약
    public StoreInfo.Summary getStoreSummary(UUID id) {
        String summaryText = storeService.getStoreSummary(id);
        if (summaryText == null || summaryText.isBlank()) {
            summaryText = "요약이 존재하지 않습니다.";
        }
        return StoreInfo.Summary.of(summaryText);
    }

    // 웹훅핸들러에서 여기로 orderId옴.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void acceptedOrder(UUID orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            storeService.acceptFromConfirmed(order);
            publishStatusChanged(order, "주문 수락이 완료되었습니다.");
            eventPublisher.publishEvent(new StoreOrderCompletedEvent(order.getStoreId()));
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new CustomException(ErrorCode.VERSION_CONFLICT);
        }
    }

    @Transactional
    public StoreInfo.OrderStatusResult preparedOrder(User user,UUID orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            Store store = storeService.findByStoreId(order.getStoreId());
            verifyOwnerOrAdmin(user, store);
            storeService.prepareFromAccepted(order);
            publishStatusChanged(order, "조리가 완료되었습니다.");
            return StoreInfo.OrderStatusResult.from(order);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new CustomException(ErrorCode.VERSION_CONFLICT);
        }
    }

    @Transactional
    public StoreInfo.OrderStatusResult pickedUpOrder(User user,UUID orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            Store store = storeService.findByStoreId(order.getStoreId());
            verifyOwnerOrAdmin(user, store);
            storeService.pickupFromPrepared(order);
            publishStatusChanged(order, "음식이 픽업되었습니다.");
            return StoreInfo.OrderStatusResult.from(order);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new CustomException(ErrorCode.VERSION_CONFLICT);
        }
    }

    @Transactional
    public StoreInfo.OrderStatusResult deliveredOrder(User user,UUID orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            Store store = storeService.findByStoreId(order.getStoreId());
            verifyOwnerOrAdmin(user, store);
            storeService.deliverFromPickedUp(order);
            publishStatusChanged(order, "배달이 완료되었습니다.");
            return StoreInfo.OrderStatusResult.from(order);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new CustomException(ErrorCode.VERSION_CONFLICT);
        }
    }

    private void publishStatusChanged(Order order, String message) {
        eventPublisher.publishEvent(
            StoreEvent.StatusChanged.of(
                order.getUserId(),
                order.getOrderId(),
                order.getOrderStatus(),
                message
            )
        );
    }

    private boolean isAdmin(User user) {
        UserRole role = user.getRole();
        return role == UserRole.MANAGER || role == UserRole.MASTER;
    }

    private void verifyOwnerOrAdmin(User user, Store store) {
        if (!isAdmin(user)) store.verifyOwner(user.getUserId());
    }

    private String displayName(User user) {
        return (user.getName() != null && !user.getName().isBlank()) ? user.getName() : "user#" + user.getUserId();
    }
}