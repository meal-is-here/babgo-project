package com.babgo.order;

import com.babgo.application.order.OrderFacade;
import com.babgo.application.order.OrderInfo;
import com.babgo.application.order.OrderQueryFacade;
import com.babgo.controller.order.OrderController;
import com.babgo.domain.user.User;
import com.babgo.global.security.resolver.CurrentUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderSortTest {

    private MockMvc mockMvc;

    @Mock
    private OrderQueryFacade orderQueryFacade;

    @Mock
    private OrderFacade orderFacade;

    @InjectMocks
    private OrderController orderController;

    private User mockUser;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(userId);

        CurrentUserArgumentResolver currentUserArgumentResolver = mock(CurrentUserArgumentResolver.class);
        try {
            when(currentUserArgumentResolver.supportsParameter(any())).thenReturn(true);
            when(currentUserArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(mockUser);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(currentUserArgumentResolver)
                .build();
    }

    @Test
    void getAllOrders_DESC() throws Exception {
        var newer = new OrderInfo.OrderDetail("order-3", "가게A", 30000L, "CONFIRMED", LocalDateTime.now());
        var older  = new OrderInfo.OrderDetail("order-2", "가게B", 20000L, "CONFIRMED", LocalDateTime.now().minusDays(1));

        var output = new OrderInfo.Orders(List.of(newer, older), 0, 10, 2, 1, false);

        when(orderQueryFacade.getAllOrders(
                eq(1L),
                eq("CONFIRMED"),
                eq(0),
                eq(10),
                eq("LATEST")
        )).thenReturn(output);

        mockMvc.perform(get("/v1/orders")
                        .param("status", "CONFIRMED")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortType", "LATEST")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].orderId").value("order-3"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }
}
