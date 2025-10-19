package com.babgo.order;

import com.babgo.application.order.OrderFacade;
import com.babgo.application.order.OrderInfo;
import com.babgo.application.order.event.OrderCreatedEvent;
import com.babgo.application.order.port.CancelWindow;
import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuService;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderItem;
import com.babgo.domain.order.OrderItemService;
import com.babgo.domain.order.OrderItemSnapshot;
import com.babgo.domain.order.OrderItemValidationResult;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.order.OrderStatus;
import com.babgo.domain.store.Category;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreService;
import com.babgo.domain.store.status.StoreStatus;
import com.babgo.domain.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OrderFacade 단위 테스트")
class OrderFacadeUnitTest {

    @Mock private OrderService orderService;
    @Mock private OrderItemService orderItemService;
    @Mock private StoreService storeService;
    @Mock private MenuService menuService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CancelWindow cancelWindow;

    // ✅ 인증 유저 목
    @Mock private User user;

    @InjectMocks
    private OrderFacade orderFacade;

    private UUID storeId;
    private UUID menuId;
    private Long userId;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        menuId = UUID.randomUUID();
        userId = 1L;

        // ✅ 인증 유저 id 스텁
        when(user.getUserId()).thenReturn(userId);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Store makeStore(boolean orderable, UUID forcedStoreId) {
        Category cat = Category.of("테스트");
        LocalTime now = LocalTime.now();

        LocalTime openStart;
        LocalTime openEnd;
        if (orderable) {
            openStart = now.minusHours(6);
            openEnd   = now.plusHours(6);
        } else {
            openStart = now.minusHours(6);
            openEnd   = now.minusHours(1); // now 미포함
        }

        Store real = Store.of(
                "가게", "서울시 강남구",
                37.0, 127.0, "111111", "02-1234-5678",
                10_000, openStart, openEnd, cat
        );
        setPrivateField(real, "storeStatus", StoreStatus.OPEN);
        if (forcedStoreId != null) setPrivateField(real, "storeId", forcedStoreId);
        return real;
    }

    private Order mockOrder(UUID orderId, long totalPrice, OrderStatus status) {
        Order o = mock(Order.class);
        when(o.getOrderId()).thenReturn(orderId);
        when(o.getTotalPrice()).thenReturn(totalPrice);
        when(o.getOrderStatus()).thenReturn(status);
        return o;
    }

    private Order mockOrderWithStatus(OrderStatus status) {
        Order o = mock(Order.class);
        when(o.getOrderStatus()).thenReturn(status);
        return o;
    }

    private OrderItem mockOrderItem(UUID mId, int quantity) {
        OrderItem it = mock(OrderItem.class);
        when(it.getMenuId()).thenReturn(mId);
        when(it.getQuantity()).thenReturn(quantity);
        return it;
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrderTests {

        @Test
        @DisplayName("성공: 재고/검증 OK → 총액 계산, 아이템 매핑, 이벤트 발행")
        void success() {
            OrderInfo.OrderItemDetail item =
                    new OrderInfo.OrderItemDetail(menuId, null, 5_000L, 2);

            // ✅ 생성자 인자 5개 일치
            List<OrderInfo.OrderItemDetail> items = List.of(item);
            OrderInfo.Create input = new OrderInfo.Create(
                    storeId, userId, "요청없음", "서울 강남구", items
            );

            UUID newOrderId = UUID.randomUUID();
            when(orderService.createOrderId()).thenReturn(newOrderId);

            when(storeService.findByStoreId(storeId))
                    .thenReturn(makeStore(true, storeId));

            OrderItemSnapshot snap = mock(OrderItemSnapshot.class);
            when(snap.getMenuId()).thenReturn(menuId);
            when(snap.getUnitPrice()).thenReturn(5_000L);
            when(snap.getQuantity()).thenReturn(2);
            when(snap.lineTotal()).thenReturn(10_000L);

            OrderItemValidationResult validation = mock(OrderItemValidationResult.class);
            when(validation.hasInvalid()).thenReturn(false);
            when(validation.getValidItems()).thenReturn(List.of(snap));
            when(orderItemService.reserveStockAndCreateOrderItems(input.getItems()))
                    .thenReturn(validation);

            Order pending = mockOrder(newOrderId, 10_000L, OrderStatus.PENDING);
            when(orderService.create(any(Order.class))).thenReturn(pending);

            OrderItem savedItem = mockOrderItem(menuId, 2);
            when(orderItemService.create(eq(List.of(snap)), same(pending)))
                    .thenReturn(List.of(savedItem));

            when(menuService.findAllByIds(List.of(menuId))).thenReturn(Collections.emptyList());

            try (MockedStatic<OrderInfo.Item> mocked = Mockito.mockStatic(OrderInfo.Item.class)) {
                mocked.when(() -> OrderInfo.Item.from(any(OrderItem.class), any()))
                        .thenAnswer(inv -> mock(OrderInfo.Item.class));

                // ✅ 인증 유저 인자 추가
                OrderInfo.CreateResult result = orderFacade.createOrder(user, input);

                assertThat(result.isOk()).isTrue();
                assertThat(result.getOrderId()).isEqualTo(newOrderId);
                assertThat(result.getTotalPrice()).isEqualTo(10_000L);
                assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING.name());
                assertThat(result.getItems()).hasSize(1);
                assertThat(result.getInvalidItems()).isEmpty();

                verify(storeService).findByStoreId(storeId);
                verify(orderItemService).reserveStockAndCreateOrderItems(input.getItems());
                verify(orderService).create(any(Order.class));
                verify(orderItemService).create(anyList(), same(pending));
                verify(menuService).findAllByIds(List.of(menuId));
                verify(eventPublisher).publishEvent(isA(OrderCreatedEvent.class));
            }
        }

        @Test
        @DisplayName("실패: 가게 미운영 시간")
        void reject_storeClosed() {
            OrderInfo.OrderItemDetail item =
                    new OrderInfo.OrderItemDetail(menuId, null, 5_000L, 1);

            List<OrderInfo.OrderItemDetail> items = List.of(item);
            OrderInfo.Create input = new OrderInfo.Create(
                    storeId, userId, "요청", "주소", items
            );

            when(orderService.createOrderId()).thenReturn(UUID.randomUUID()); // 안전
            when(storeService.findByStoreId(storeId))
                    .thenReturn(makeStore(false, storeId)); // 닫힘

            // ✅ 인증 유저 인자 추가
            OrderInfo.CreateResult result = orderFacade.createOrder(user, input);

            assertThat(result.isOk()).isFalse();
            assertThat(result.getMessage()).isEqualTo("현재 가게 운영 시간이 아닙니다.");

            verify(storeService).findByStoreId(storeId);
            verifyNoInteractions(orderItemService, menuService, eventPublisher);
        }

        @Test
        @DisplayName("실패: 일부 메뉴가 주문 불가")
        void reject_invalidItems() {
            OrderInfo.OrderItemDetail item =
                    new OrderInfo.OrderItemDetail(menuId, null, 5_000L, 2);

            List<OrderInfo.OrderItemDetail> items = List.of(item);
            OrderInfo.Create input = new OrderInfo.Create(
                    storeId, userId, "요청", "주소", items
            );

            when(orderService.createOrderId()).thenReturn(UUID.randomUUID()); // 안전
            when(storeService.findByStoreId(storeId))
                    .thenReturn(makeStore(true, storeId)); // 열림 → 검증 단계 진입

            OrderItemValidationResult validation = mock(OrderItemValidationResult.class);
            when(validation.hasInvalid()).thenReturn(true);
            when(validation.getInvalidItems()).thenReturn(
                    List.of(new OrderInfo.InvalidItem(menuId, "MENU_UNAVAILABLE"))
            );
            when(orderItemService.reserveStockAndCreateOrderItems(any()))
                    .thenReturn(validation);

            // ✅ 인증 유저 인자 추가
            OrderInfo.CreateResult result = orderFacade.createOrder(user, input);

            assertThat(result.isOk()).isFalse();
            assertThat(result.getMessage()).isEqualTo("일부 메뉴가 주문 불가합니다.");
            assertThat(result.getInvalidItems()).hasSize(1);

            verify(storeService).findByStoreId(storeId);
            verify(orderItemService).reserveStockAndCreateOrderItems(input.getItems());
            verify(orderService, atMostOnce()).createOrderId();   // 허용
            verify(orderService, never()).create(any(Order.class));
            verifyNoInteractions(menuService, eventPublisher);
            verifyNoMoreInteractions(storeService, orderItemService);
        }

        @Test
        @DisplayName("성공: 다중 메뉴 주문")
        void success_multipleItems() {
            UUID menu2 = UUID.randomUUID();

            OrderInfo.OrderItemDetail i1 = new OrderInfo.OrderItemDetail(menuId, null, 5_000L, 2);
            OrderInfo.OrderItemDetail i2 = new OrderInfo.OrderItemDetail(menu2,  null, 3_000L, 1);

            List<OrderInfo.OrderItemDetail> items = Arrays.asList(i1, i2);
            OrderInfo.Create input = new OrderInfo.Create(
                    storeId, userId, "요청", "서울 강남구", items
            );

            UUID newOrderId = UUID.randomUUID();
            when(orderService.createOrderId()).thenReturn(newOrderId); // 반드시 스텁
            when(storeService.findByStoreId(storeId))
                    .thenReturn(makeStore(true, storeId)); // 열림

            OrderItemSnapshot s1 = mock(OrderItemSnapshot.class);
            when(s1.getMenuId()).thenReturn(menuId);
            when(s1.getUnitPrice()).thenReturn(5_000L);
            when(s1.getQuantity()).thenReturn(2);
            when(s1.lineTotal()).thenReturn(10_000L);

            OrderItemSnapshot s2 = mock(OrderItemSnapshot.class);
            when(s2.getMenuId()).thenReturn(menu2);
            when(s2.getUnitPrice()).thenReturn(3_000L);
            when(s2.getQuantity()).thenReturn(1);
            when(s2.lineTotal()).thenReturn(3_000L);

            OrderItemValidationResult validation = mock(OrderItemValidationResult.class);
            when(validation.hasInvalid()).thenReturn(false);
            when(validation.getValidItems()).thenReturn(List.of(s1, s2));
            when(orderItemService.reserveStockAndCreateOrderItems(input.getItems()))
                    .thenReturn(validation);

            Order pending = mockOrder(newOrderId, 13_000L, OrderStatus.PENDING);
            when(orderService.create(any(Order.class))).thenReturn(pending);

            OrderItem oi1 = mockOrderItem(menuId, 2);
            OrderItem oi2 = mockOrderItem(menu2, 1);
            when(orderItemService.create(eq(List.of(s1, s2)), same(pending)))
                    .thenReturn(List.of(oi1, oi2));

            when(menuService.findAllByIds(Arrays.asList(menuId, menu2))).thenReturn(Collections.emptyList());

            try (MockedStatic<OrderInfo.Item> mocked = Mockito.mockStatic(OrderInfo.Item.class)) {
                mocked.when(() -> OrderInfo.Item.from(any(OrderItem.class), any()))
                        .thenAnswer(inv -> mock(OrderInfo.Item.class));

                // ✅ 인증 유저 인자 추가
                OrderInfo.CreateResult result = orderFacade.createOrder(user, input);

                assertThat(result.isOk()).isTrue();
                assertThat(result.getOrderId()).isEqualTo(newOrderId);
                assertThat(result.getTotalPrice()).isEqualTo(13_000L);
                assertThat(result.getItems()).hasSize(2);
            }
        }
    }

    @Nested
    @DisplayName("취소 윈도우")
    class CancelOrderTests {

        @Test
        @DisplayName("성공: PENDING → 취소, 재고 복구, 취소 윈도우 닫힘")
        void pending_success() {
            UUID orderId = UUID.randomUUID();
            when(cancelWindow.isOpen(orderId)).thenReturn(true);

            Order order = mockOrderWithStatus(OrderStatus.PENDING);
            when(orderService.getOrder(orderId)).thenReturn(order);

            UUID m1 = UUID.randomUUID();
            UUID m2 = UUID.randomUUID();
            OrderItem i1 = mockOrderItem(m1, 2);
            OrderItem i2 = mockOrderItem(m2, 1);
            when(orderService.findAllOrderItem(orderId)).thenReturn(List.of(i1, i2));

            OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getMessage()).contains("취소했습니다");

            verify(orderService).updateCancel(order);
            verify(orderService).findAllOrderItem(orderId);
            verify(menuService).increaseStock(m1, 2);
            verify(menuService).increaseStock(m2, 1);
            verify(cancelWindow).close(orderId);
        }

        @Test
        @DisplayName("거절: 취소 윈도우 닫힘")
        void window_closed() {
            UUID orderId = UUID.randomUUID();
            when(cancelWindow.isOpen(orderId)).thenReturn(false);

            OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId);

            assertThat(result.isOk()).isFalse();
            assertThat(result.getMessage()).contains("만료");

            verify(cancelWindow).isOpen(orderId);
            verifyNoInteractions(orderService, menuService);
        }

        @Test
        @DisplayName("PAYMENT_IN_PROGRESS: 취소요청 전환")
        void payment_in_progress() {
            UUID orderId = UUID.randomUUID();
            when(cancelWindow.isOpen(orderId)).thenReturn(true);

            Order order = mockOrderWithStatus(OrderStatus.PAYMENT_IN_PROGRESS);
            when(orderService.getOrder(orderId)).thenReturn(order);

            OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getMessage()).contains("결제 취소를 요청했습니다");

            verify(orderService).updateCancelRequested(order);
            verify(cancelWindow).close(orderId);
        }

        @Test
        @DisplayName("CONFIRMED: 환불요청 전환")
        void confirmed_refund_requested() {
            UUID orderId = UUID.randomUUID();
            when(cancelWindow.isOpen(orderId)).thenReturn(true);

            Order order = mockOrderWithStatus(OrderStatus.CONFIRMED);
            when(orderService.getOrder(orderId)).thenReturn(order);

            OrderInfo.CancelResult result = orderFacade.cancelOrder(orderId);

            assertThat(result.isOk()).isTrue();
            assertThat(result.getMessage()).contains("환불을 요청했습니다");

            verify(orderService).updateRefundRequested(order);
            verify(cancelWindow).close(orderId);
        }
    }
}
