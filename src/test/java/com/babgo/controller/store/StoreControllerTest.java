package com.babgo.controller.store;

import com.babgo.application.store.StoreFacade;
import com.babgo.application.store.StoreInfo;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderStatus;
import com.babgo.global.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(StoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreFacade storeFacade;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    AuditorAware<String> auditorAware;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("POST /v1/stores - 가게등록 성공")
    @Test
    void createStore() throws Exception {
        StoreRequest.Upsert request = StoreRequest.Upsert.of(
                "버거프렌즈",
                "서울시 강남구 테헤란로 123",
                37.4979,
                127.0276,
                "123456",
                "010-1234-5678",
                12000,
                LocalTime.of(9, 0),
                LocalTime.of(21, 0),
                UUID.randomUUID()
        );

        mockMvc.perform(
                        post("/v1/stores")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가게 등록을 성공했습니다."));

        verify(storeFacade, times(1)).createStore(any(StoreInfo.Create.class));
    }

    @DisplayName("PATCH /v1/stores/{storeId} - 가게수정 성공")
    @Test
    void updateStore() throws Exception {
        UUID storeId = UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("storeName", "버거프렌즈 민수점");
        body.put("addressLine", "서울시 강남구 나민수남로 123");
        body.put("latitude", 56.00);
        body.put("longitude", 125.322);

        mockMvc.perform(
                        patch("/v1/stores/{storeId}", storeId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가게 수정을 성공했습니다."));

        verify(storeFacade, times(1)).updateStore(eq(storeId), any(StoreInfo.Update.class));
    }

    @DisplayName("DELETE /v1/stores/{storeId} - 가게삭제 성공")
    @Test
    void deleteStore_success() throws Exception {
        UUID storeId = UUID.randomUUID();

        mockMvc.perform(
                        delete("/v1/stores/{storeId}", storeId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("가게 삭제를 성공했습니다."));

        verify(storeFacade, times(1)).deleteStore(eq(storeId));
    }

    @DisplayName("PATCH /v1/stores/orders/{orderId}/prepared -> 성공 응답과 메시지/데이터를 반환")
    @Test
    void prepared_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PREPARED);

        StoreInfo.OrderStatusResult output = StoreInfo.OrderStatusResult.from(order);

        when(storeFacade.preparedOrder(eq(orderId))).thenReturn(output);
        // when & then
        mockMvc.perform(patch("/v1/stores/orders/{orderId}/prepared", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("조리 완료 되었습니다."))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.status").value("PREPARED"));

        verify(storeFacade, times(1)).preparedOrder(eq(orderId));
    }

    @DisplayName("PATCH /v1/stores/orders/{orderId}/picked-up -> 성공 응답과 메시지/데이터를 반환")
    @Test
    void picked_up_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PICKED_UP);

        StoreInfo.OrderStatusResult output = StoreInfo.OrderStatusResult.from(order);
        when(storeFacade.pickedUpOrder(eq(orderId))).thenReturn(output);

        // when & then
        mockMvc.perform(patch("/v1/stores/orders/{orderId}/picked-up", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("음식이 픽업되었습니다."))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.status").value("PICKED_UP"));

        verify(storeFacade, times(1)).pickedUpOrder(eq(orderId));
    }

    @DisplayName("PATCH /v1/stores/orders/{orderId}/delivered -> 성공 응답과 메시지/데이터를 반환")
    @Test
    void delivered_success() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();

        Order order = mock(Order.class);
        when(order.getOrderId()).thenReturn(orderId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.DELIVERED);

        StoreInfo.OrderStatusResult output = StoreInfo.OrderStatusResult.from(order);
        when(storeFacade.deliveredOrder(eq(orderId))).thenReturn(output);

        // when & then
        mockMvc.perform(patch("/v1/stores/orders/{orderId}/delivered", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("배달이 완료되었습니다."))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));

        verify(storeFacade, times(1)).deliveredOrder(eq(orderId));
    }
}
