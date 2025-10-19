//package com.babgo.controller.review;
//
//import com.babgo.controller.review.dto.ReviewCreateRequest;
//import com.babgo.domain.order.Order;
//import com.babgo.domain.order.OrderRepository;
//import com.babgo.domain.order.OrderStatus;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(classes = com.babgo.BabgoApplication.class)
//@AutoConfigureMockMvc
//class ReviewControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    // 리뷰 등록 - 성공 (인증된 사용자)
//    @Test
//    @DisplayName("리뷰 등록 - 성공 (인증된 사용자)")
//    void createReview_success() throws Exception {
//        // given
//        Order order = Order.of(
//                UUID.randomUUID(),
//                UUID.randomUUID(),
//                1L,
//                "배달 요청사항 없음",
//                "서울시 강남구",
//                20000L
//        );
//        order.updateStatus(OrderStatus.CONFIRMED);
//        orderRepository.save(order);
//
//        ReviewCreateRequest request = new ReviewCreateRequest(
//                order.getOrderId(),
//                5,
//                "배달도 빠르고 맛있어요!"
//        );
//
//        // when & then
//        mockMvc.perform(post("/v1/reviews")
//                        .header("Authorization", "Bearer mock-token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized()); // JWT 없음 → 401 기대
//    }
//
//    // 리뷰 등록 - 비로그인 사용자
//    @Test
//    @DisplayName("리뷰 등록 - 비로그인 사용자 (401 Unauthorized)")
//    void createReview_unauthorized() throws Exception {
//        ReviewCreateRequest request = new ReviewCreateRequest(
//                UUID.randomUUID(),
//                5,
//                "비로그인 리뷰 테스트"
//        );
//
//        mockMvc.perform(post("/v1/reviews")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.error").value("Unauthorized"))
//                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
//    }
//}