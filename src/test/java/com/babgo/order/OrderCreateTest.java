package com.babgo.order;

import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderStatus;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.*;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class OrderCreateTest {

    UUID orderId;
    UUID storeId = UUID.randomUUID();
    Long userId = 1L;


    @BeforeEach
    void setUp() {
        orderId = UuidCreator.getTimeOrdered();
    }

    @Nested
    @DisplayName("성공")
    class Success {

        @Test
        @DisplayName("주문 정보 초기 생성")
        void initPending() {
            Order order = Order.of(
                    orderId,
                    storeId,
                    userId,
                    "리뷰 이벤트 참가할게요",
                    "남문리",
                    1L
            );

            Assertions.assertEquals(OrderStatus.PENDING,order.getOrderStatus(),"pending 상태의 초기 주문 정보 생성 실패");
        }
    }


    @Nested
    @DisplayName("검증 실패")
    class False {

        @Test
        @DisplayName("가게 정보")
        void store(){
            assertThatThrownBy(() ->
                    Order.of(orderId, null, userId,
                            "요청", "서울", 1000L)
            ).isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID);
        }


        @Test
        @DisplayName("유저 정보")
        void user(){
            assertThatThrownBy(() ->
                    Order.of(orderId, storeId, null,
                            "요청", "서울", 1000L)
            ).isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID);
        }

        @Test
        @DisplayName("가격 정보")
        void toralPrice(){
            assertThatThrownBy(() ->
                    Order.of(orderId, storeId, userId,
                            "요청", "서울", -10L)
            ).isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID);
        }

        @Test
        @DisplayName("배달 주소 빈 값")
        void addressEmpty (){
            assertThatThrownBy(() ->
                    Order.of(orderId, storeId, userId,
                            "요청", null, 1000L)
            ).isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID);
        }

        @Test
        @DisplayName("배달 주소 null")
        void address (){
            assertThatThrownBy(() ->
                    Order.of(orderId, storeId, userId,
                            "요청", "", 1000L)
            ).isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID);
        }
    }
}
