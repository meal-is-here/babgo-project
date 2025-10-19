package com.babgo.application.store;

import com.babgo.application.store.event.StoreEvent;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryService;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreFacadeTest {

    @Mock StoreService storeService;
    @Mock CategoryService categoryService;
    @Mock OrderService orderService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks StoreFacade storeFacade;

    @DisplayName("카테고리를 조회해 Store를 생성하고, storeService.create()를 호출한다.")
    @Test
    void createStore_success() {
        // given
        UUID categoryId = UUID.randomUUID();
        User user = mockOwnerUserWithName();
        StoreInfo.Create input = StoreInfo.Create.of(
                "홍대 김치찌개", "서울시 마포구 양화로 123",
                37.5665, 126.9780, "123456", "02-1234-5678", 15000,
                LocalTime.of(9, 0), LocalTime.of(21, 0), categoryId
        );
        Category category = mock(Category.class);
        when(categoryService.findByCategoryId(categoryId)).thenReturn(category);

        // when
        storeFacade.createStore(user, input);

        // then
        verify(categoryService).findByCategoryId(categoryId);
        ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).create(storeCaptor.capture(), eq("userName"));

        Store created = storeCaptor.getValue();
        assertThat(created).isNotNull();
        assertThat(created.getCategory()).isSameAs(category);
        assertThat(created.getStoreName()).isEqualTo("홍대 김치찌개");
        assertThat(created.getAddressLine()).isEqualTo("서울시 마포구 양화로 123");
        assertThat(created.getLatitude()).isEqualTo(37.5665);
        assertThat(created.getLongitude()).isEqualTo(126.9780);
        assertThat(created.getRegionCode()).isEqualTo("123456");
        assertThat(created.getPhoneNumber()).isEqualTo("02-1234-5678");
        assertThat(created.getMinOrderAmount()).isEqualTo(15000);
        assertThat(created.getOpeningHours()).isEqualTo(LocalTime.of(9, 0));
        assertThat(created.getClosingHours()).isEqualTo(LocalTime.of(21, 0));

        verifyNoMoreInteractions(storeService, categoryService);
    }

    @DisplayName("updateStore: 변경값만 Map에 담아 storeService.update() 호출")
    @Test
    void updateStore_success() {
        // given
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        User user = mockOwnerUserWithName();
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        UUID newCategoryId = UUID.randomUUID();
        StoreInfo.Update input = StoreInfo.Update.of(
                "새로운 가게명", "서울시 어딘가 123",
                37.5, 129.0, "123458",
                null, 20000,
                LocalTime.of(9, 0), LocalTime.of(22, 0),
                newCategoryId
        );

        // when
        storeFacade.updateStore(user, storeId, input);

        // then
        verify(storeService).findByStoreId(storeId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(storeService).update(same(store), mapCaptor.capture(), eq("userName"));

        Map<String, Object> changes = mapCaptor.getValue();
        assertThat(changes).containsEntry("storeName", "새로운 가게명");
        assertThat(changes).containsEntry("addressLine", "서울시 어딘가 123");
        assertThat(changes).containsEntry("latitude", 37.5);
        assertThat(changes).containsEntry("longitude", 129.0);
        assertThat(changes).containsEntry("regionCode", "123458");
        assertThat(changes).doesNotContainKey("phoneNumber");
        assertThat(changes).containsEntry("minOrderAmount", 20000);
        assertThat(changes).containsEntry("openingHours", LocalTime.of(9, 0));
        assertThat(changes).containsEntry("closingHours", LocalTime.of(22, 0));
        assertThat(changes).containsEntry("categoryId", newCategoryId);

        verifyNoMoreInteractions(storeService);
        verifyNoInteractions(categoryService);
    }

    @DisplayName("deleteStore: Store를 찾아서 storeService.delete() 호출")
    @Test
    void deleteStore_success() {
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        User user = mockOwnerUserWithName();
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        storeFacade.deleteStore(user, storeId);

        verify(storeService).findByStoreId(storeId);
        verify(storeService).delete(same(store), eq("userName"));
        verifyNoMoreInteractions(storeService);
        verifyNoInteractions(categoryService);
    }

    @DisplayName("deleteStore: service 예외 그대로 전파")
    @Test
    void deleteStore_throws_from_service() {
        UUID storeId = UUID.randomUUID();
        Store store = mock(Store.class);
        User user = mockOwnerUserWithName();
        when(storeService.findByStoreId(storeId)).thenReturn(store);
        doThrow(new CustomException(ErrorCode.BAD_REQUEST, "이미 삭제된 가게입니다."))
                .when(storeService).delete(same(store), eq("userName"));

        CustomException ex = assertThrows(CustomException.class,
                () -> storeFacade.deleteStore(user, storeId));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        assertThat(ex.getCustomMessage()).isEqualTo("이미 삭제된 가게입니다.");

        verify(storeService).findByStoreId(storeId);
        verify(storeService).delete(same(store), eq("userName"));
        verifyNoMoreInteractions(storeService);
        verifyNoInteractions(categoryService);
    }

    @DisplayName("acceptedOrder: CONFIRMED→ACCEPTED 전이 + 이벤트 발행")
    @Test
    void acceptedOrder_success() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);

        when(orderService.getOrder(orderId)).thenReturn(order);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getUserId()).thenReturn(1L);
        when(order.getOrderStatus()).thenReturn(OrderStatus.ACCEPTED);

        storeFacade.acceptedOrder(orderId);

        verify(orderService).getOrder(orderId);
        verify(storeService).acceptFromConfirmed(order);

        ArgumentCaptor<Object> evt = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(evt.capture());
        StoreEvent.StatusChanged changed = (StoreEvent.StatusChanged) evt.getValue();
        assertThat(changed.userId()).isEqualTo(1L);
        assertThat(changed.orderId()).isEqualTo(orderId);
        assertThat(changed.status()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(changed.message()).contains("주문 수락이 완료");
    }

    @DisplayName("acceptOrder: 낙관적락 충돌 시 VERSION_CONFLICT(409)")
    @Test
    void acceptOrder_versionConflict() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        when(orderService.getOrder(orderId)).thenReturn(order);
        doThrow(new ObjectOptimisticLockingFailureException(Order.class, orderId))
                .when(storeService).acceptFromConfirmed(order);

        assertThatThrownBy(() -> storeFacade.acceptedOrder(orderId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VERSION_CONFLICT);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("preparedOrder: ACCEPTED→PREPARED 전이 + 이벤트 발행")
    @Test
    void preparedOrder_success() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        User user = mockOwnerUserMinimal();

        UUID storeId = UUID.randomUUID();
        when(order.getStoreId()).thenReturn(storeId);
        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        when(orderService.getOrder(orderId)).thenReturn(order);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getUserId()).thenReturn(1L);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PREPARED);

        StoreInfo.OrderStatusResult result = storeFacade.preparedOrder(user, orderId);

        verify(orderService).getOrder(orderId);
        verify(storeService).findByStoreId(storeId);
        verify(store).verifyOwner(1L);
        verify(storeService).prepareFromAccepted(order);

        ArgumentCaptor<Object> evt = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(evt.capture());
        StoreEvent.StatusChanged changed = (StoreEvent.StatusChanged) evt.getValue();
        assertThat(changed.status()).isEqualTo(OrderStatus.PREPARED);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PREPARED);
    }

    @DisplayName("preparedOrder: 낙관적락 충돌 시 VERSION_CONFLICT(409)")
    @Test
    void preparedOrder_versionConflict() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        User user = mockOwnerUserMinimal();

        UUID storeId = UUID.randomUUID();
        when(order.getStoreId()).thenReturn(storeId);
        when(storeService.findByStoreId(storeId)).thenReturn(mock(Store.class));
        when(orderService.getOrder(orderId)).thenReturn(order);

        doThrow(new ObjectOptimisticLockingFailureException(Order.class, orderId))
                .when(storeService).prepareFromAccepted(order);

        assertThatThrownBy(() -> storeFacade.preparedOrder(user, orderId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VERSION_CONFLICT);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("pickedUpOrder: PREPARED→PICKED_UP 전이 + 이벤트 발행")
    @Test
    void pickedUpOrder_success() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        User user = mockOwnerUserMinimal();

        UUID storeId = UUID.randomUUID();
        when(order.getStoreId()).thenReturn(storeId);
        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        when(orderService.getOrder(orderId)).thenReturn(order);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getUserId()).thenReturn(1L);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PICKED_UP);

        StoreInfo.OrderStatusResult result = storeFacade.pickedUpOrder(user, orderId);

        verify(orderService).getOrder(orderId);
        verify(storeService).findByStoreId(storeId);
        verify(store).verifyOwner(1L);
        verify(storeService).pickupFromPrepared(order);

        ArgumentCaptor<Object> evt = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(evt.capture());
        StoreEvent.StatusChanged changed = (StoreEvent.StatusChanged) evt.getValue();
        assertThat(changed.status()).isEqualTo(OrderStatus.PICKED_UP);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PICKED_UP);
    }

    @DisplayName("pickedUpOrder: 낙관적락 충돌 시 VERSION_CONFLICT(409)")
    @Test
    void pickedUpOrder_versionConflict() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        User user = mockOwnerUserMinimal();

        UUID storeId = UUID.randomUUID();
        when(order.getStoreId()).thenReturn(storeId);
        when(storeService.findByStoreId(storeId)).thenReturn(mock(Store.class));
        when(orderService.getOrder(orderId)).thenReturn(order);

        doThrow(new ObjectOptimisticLockingFailureException(Order.class, orderId))
                .when(storeService).pickupFromPrepared(order);

        assertThatThrownBy(() -> storeFacade.pickedUpOrder(user, orderId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VERSION_CONFLICT);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("deliveredOrder: PICKED_UP→DELIVERED 전이 + 이벤트 발행")
    @Test
    void deliveredOrder_success() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        User user = mockOwnerUserMinimal();

        UUID storeId = UUID.randomUUID();
        when(order.getStoreId()).thenReturn(storeId);
        Store store = mock(Store.class);
        when(storeService.findByStoreId(storeId)).thenReturn(store);

        when(orderService.getOrder(orderId)).thenReturn(order);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getUserId()).thenReturn(1L);
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);

        StoreInfo.OrderStatusResult result = storeFacade.deliveredOrder(user, orderId);

        verify(orderService).getOrder(orderId);
        verify(storeService).findByStoreId(storeId);
        verify(store).verifyOwner(1L);
        verify(storeService).deliverFromPickedUp(order);

        ArgumentCaptor<Object> evt = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(evt.capture());
        StoreEvent.StatusChanged changed = (StoreEvent.StatusChanged) evt.getValue();
        assertThat(changed.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @DisplayName("deliveredOrder: 낙관적락 충돌 시 VERSION_CONFLICT(409)")
    @Test
    void deliveredOrder_versionConflict() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);
        User user = mockOwnerUserMinimal();

        UUID storeId = UUID.randomUUID();
        when(order.getStoreId()).thenReturn(storeId);
        when(storeService.findByStoreId(storeId)).thenReturn(mock(Store.class));
        when(orderService.getOrder(orderId)).thenReturn(order);

        doThrow(new ObjectOptimisticLockingFailureException(Order.class, orderId))
                .when(storeService).deliverFromPickedUp(order);

        assertThatThrownBy(() -> storeFacade.deliveredOrder(user, orderId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.VERSION_CONFLICT);

        verify(eventPublisher, never()).publishEvent(any());
    }

    // ---------- helpers ----------

    /** 생성/수정/삭제에서 displayName 사용 → name 필요 */
    private User mockOwnerUserWithName() {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        when(user.getName()).thenReturn("userName");
        return user;
    }

    /** 상태변경 테스트에선 name 불필요 → 불필요 스텁 제거 */
    private User mockOwnerUserMinimal() {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(1L);
        return user;
    }
}
