package com.babgo.application.store;


import com.babgo.application.store.event.StoreEvent;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
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
    public void createStore(StoreInfo.Create input) {
        Category category = categoryService.findByCategoryId(input.getCategoryId());
        Store store = Store.of(
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
        storeService.create(store, "userName");
    }

    @Transactional
    public void updateStore(UUID storeId, StoreInfo.Update input) {
        Store store = storeService.findByStoreId(storeId);

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

        storeService.update(store, changes, "userName");
    }

    @Transactional
    public void deleteStore(UUID storeId) {
        Store store = storeService.findByStoreId(storeId);
        storeService.delete(store, "userName");
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
            log.info("이벤트================================"+orderId);

            Order order = orderService.getOrder(orderId);
            Long userId = order.getUserId();
            // Store에 ownerId 필드 추가해야함 인증되면 ->
            // Store store = storeService.findByStoreId(order.getStoreId) ->
            // store.getOwnerId 와 인증객체 id랑 같은지 비교/ 이유: 사장님이 수락버튼 누른다고 가정 userId == store.getOwnerId
            storeService.acceptFromConfirmed(order);
            publishStatusChanged(order, "주문 수락이 완료되었습니다.");
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new CustomException(ErrorCode.VERSION_CONFLICT);
        }
    }

    @Transactional
    public StoreInfo.OrderStatusResult preparedOrder(UUID orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            // Store에 ownerId 필드 추가해야함! 인증되면 ->
            // Store store = storeService.findByStoreId(order.getStoreId) ->
            // store.getOwnerId 와 인증객체 id랑 같은지 비교/ 이유: 사장님이 수락버튼 누른다고 가정 userId == store.getOwnerId
            storeService.prepareFromAccepted(order);
            publishStatusChanged(order, "조리가 완료되었습니다.");
            return StoreInfo.OrderStatusResult.from(order);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new CustomException(ErrorCode.VERSION_CONFLICT);
        }
    }

    @Transactional
    public StoreInfo.OrderStatusResult pickedUpOrder(UUID orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            // Store에 ownerId 필드 추가해야함! 인증되면 ->
            // Store store = storeService.findByStoreId(order.getStoreId) ->
            // store.getOwnerId 와 인증객체 id랑 같은지 비교/ 이유: 사장님이 수락버튼 누른다고 가정 userId == store.getOwnerId
            storeService.pickupFromPrepared(order);
            publishStatusChanged(order, "음식이 픽업되었습니다.");
            return StoreInfo.OrderStatusResult.from(order);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new CustomException(ErrorCode.VERSION_CONFLICT);
        }
    }

    @Transactional
    public StoreInfo.OrderStatusResult deliveredOrder(UUID orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            // Store에 ownerId 필드 추가해야함! 인증되면 ->
            // Store store = storeService.findByStoreId(order.getStoreId) ->
            // store.getOwnerId 와 인증객체 id랑 같은지 비교/ 이유: 사장님이 수락버튼 누른다고 가정 userId == store.getOwnerId
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
}