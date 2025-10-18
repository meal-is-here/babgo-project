package com.babgo.order;

import com.babgo.application.order.OrderFacade;
import com.babgo.application.order.OrderInfo;
import com.babgo.application.order.port.CancelWindow;
import com.babgo.domain.menu.MenuService;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderItemService;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.store.Category;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import com.babgo.domain.store.status.StoreStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order & Payment 단위 테스트")
public class OrderPaymentUnitTest {

    @Mock private OrderService orderService;
    @Mock private OrderItemService orderItemService;
    @Mock private StoreService storeService;
    @Mock private MenuService menuService;
    @Mock private CancelWindow cancelWindow;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderFacade orderFacade;

    private UUID testStoreId;
    private UUID testMenuId;

    @BeforeEach
    void setUp() {
        testStoreId = UUID.randomUUID();
        testMenuId = UUID.randomUUID();
    }

    private Store makeStore(boolean orderable, UUID forcedStoreId) {
        Category cat = Category.of("테스트");
        LocalTime now = LocalTime.now();

        LocalTime openStart = now.minusHours(6);
        LocalTime openEnd   = orderable ? now.plusHours(6) : now.minusHours(1); // 닫힘이면 now가 범위 밖

        Store real = Store.of(
                "가게", "서울시 강남구",
                37.0, 127.0, "111111", "02-1234-5678",
                10_000, openStart, openEnd, cat
        );

        try {
            var status = Store.class.getDeclaredField("storeStatus");
            status.setAccessible(true);
            status.set(real, StoreStatus.OPEN);
            var idf = Store.class.getDeclaredField("storeId");
            idf.setAccessible(true);
            idf.set(real, forcedStoreId);
        } catch (Exception ignored) {}
        return real;
    }

    @Test
    @DisplayName("성공: 주문 생성")
    void createOrder_success() {
        UUID orderId = UUID.randomUUID();
        OrderInfo.OrderItemDetail itemDetail =
                new OrderInfo.OrderItemDetail(testMenuId, null, 5000L, 2);
        OrderInfo.Create input =
                new OrderInfo.Create(testStoreId, 1L, "요청", "주소", List.of(itemDetail));

        when(orderService.createOrderId()).thenReturn(orderId);
        when(storeService.findByStoreId(testStoreId)).thenReturn(makeStore(true, testStoreId));

        var validationResult = mock(com.babgo.domain.order.OrderItemValidationResult.class);
        when(validationResult.hasInvalid()).thenReturn(false);
        when(validationResult.getValidItems()).thenReturn(List.of());
        when(orderItemService.reserveStockAndCreateOrderItems(any())).thenReturn(validationResult);

        Order pending = mock(Order.class);
        when(pending.getOrderId()).thenReturn(orderId);
        when(pending.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        when(orderService.create(any(Order.class))).thenReturn(pending);

        OrderInfo.CreateResult result = orderFacade.createOrder(input);

        assertThat(result.isOk()).isTrue();
        verify(orderService).createOrderId();
        verify(storeService).findByStoreId(testStoreId);
        verify(orderItemService).reserveStockAndCreateOrderItems(input.getItems());
        verify(orderService).create(any(Order.class));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        Object published = eventCaptor.getValue();
        assertThat(published).isInstanceOf(com.babgo.application.order.event.OrderCreatedEvent.class);

    }

    @Test
    @DisplayName("실패: 주문 불가능한 시간")
    void createOrder_fail_store_closed() {
        OrderInfo.OrderItemDetail itemDetail =
                new OrderInfo.OrderItemDetail(testMenuId, null, 5000L, 1);
        OrderInfo.Create input =
                new OrderInfo.Create(testStoreId, 1L, "요청", "주소", List.of(itemDetail));

        when(orderService.createOrderId()).thenReturn(UUID.randomUUID());
        when(storeService.findByStoreId(testStoreId)).thenReturn(makeStore(false, testStoreId));

        OrderInfo.CreateResult result = orderFacade.createOrder(input);

        assertThat(result.isOk()).isFalse();
        assertThat(result.getMessage()).contains("운영 시간");

        verify(orderItemService, never()).reserveStockAndCreateOrderItems(any());
        verify(orderService, never()).create(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("성공: 주문 취소 - PENDING 상태")
    void cancelOrder_success() {
        UUID orderId = UUID.randomUUID();
        when(cancelWindow.isOpen(orderId)).thenReturn(true);

        Order pendingOrder = mock(Order.class);
        when(pendingOrder.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        when(orderService.getOrder(orderId)).thenReturn(pendingOrder);

        OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId);

        assertThat(result.isOk()).isTrue();
        verify(orderService).updateCancel(pendingOrder);
        verify(cancelWindow).close(orderId);
    }

    @Test
    @DisplayName("실패: 취소 가능 시간 만료")
    void cancelOrder_fail_window_closed() {
        UUID orderId = UUID.randomUUID();
        when(cancelWindow.isOpen(orderId)).thenReturn(false);

        OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId);

        assertThat(result.isOk()).isFalse();
        verify(orderService, never()).updateCancel(any());
        verify(orderService, never()).getOrder(any());
    }
}
