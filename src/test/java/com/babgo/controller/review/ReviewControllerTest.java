package com.babgo.controller.review;

import com.babgo.controller.review.dto.ReviewCreateRequest;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderRepository;
import com.babgo.domain.review.Review;
import com.babgo.domain.review.ReviewRepository;
import com.babgo.domain.store.Category;
import com.babgo.domain.store.CategoryRepository;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.babgo.BabgoApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store store;

    @BeforeEach
    void setUp() {
        // given
        Category fakeCategory = Category.of("국밥");
        categoryRepository.save(fakeCategory);

        store = Store.of(
                "담소",
                "서울시 강서구 마곡동",
                37.123,
                127.456,
                "11",
                "010-1234-5678",
                15000,
                LocalTime.of(9, 0),
                LocalTime.of(22, 0),
                fakeCategory
        );
        store.markCreateBy("testUser");
        storeRepository.save(store);

        Review review1 = Review.of(5, "아주 맛있어요", 1L, store.getStoreId(), UUID.randomUUID());
        Review review2 = Review.of(3, "보통이에요", 2L, store.getStoreId(), UUID.randomUUID());
        reviewRepository.save(review1);
        reviewRepository.save(review2);
    }

    @Test
    @DisplayName("리뷰 등록 - 성공 (인증된 사용자)")
    void createReview_success() throws Exception {
        // given
        Order order = Order.of(
                UUID.randomUUID(),
                store.getStoreId(),
                1L,
                "문 앞에 두고 노크해주세요.",
                "서울시 강서구",
                20000L
        );
        order.markConfirmed();
        orderRepository.save(order);

        ReviewCreateRequest request = new ReviewCreateRequest(
                order.getOrderId(),
                5,
                "배달도 빠르고 맛있어요!"
        );

        // when & then
        mockMvc.perform(post("/v1/reviews")
                        .header("Authorization", "Bearer mock-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("리뷰 등록 성공"));
    }

// TODO: JWT 비활성화 해제 후 사용 예정

//    @Test
//    @DisplayName("리뷰 등록 - 실패 (401 Unauthorized)")
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
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
//    }

    @Test
    @DisplayName("음식점별 리뷰 조회 - 성공 (최신순 기본)")
    void getReviewsByStore_success() throws Exception {
        mockMvc.perform(get("/v1/reviews/{storeId}", store.getStoreId())
                        .param("sort", "latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("리뷰 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("음식점별 리뷰 조회 - 실패 (존재하지 않는 가게)")
    void getReviewsByStore_notFound() throws Exception {
        UUID invalidStoreId = UUID.randomUUID();

        mockMvc.perform(get("/v1/reviews/{storeId}", invalidStoreId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 가게입니다."));
    }

    @Test
    @DisplayName("음식점별 리뷰 조회 - 실패 (유효하지 않은 정렬 조건)")
    void getReviewsByStore_invalidSort() throws Exception {
        mockMvc.perform(get("/v1/reviews/{storeId}", store.getStoreId())
                        .param("sort", "wrong")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 정렬 조건입니다."));
    }


    private void setAuthPrincipal(Long userId) {
        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, List.of())
        );
    }

    @Test
    @DisplayName("사용자별 리뷰 조회 - 성공 (최신순 기본)")
    void getMyReviews_success_latest() throws Exception {
        // given
        setAuthPrincipal(1L);

        // when & then
        mockMvc.perform(get("/v1/reviews")
                        .param("sort", "latest")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 리뷰 조회 성공"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("사용자별 리뷰 조회 - 실패 (잘못된 정렬 조건)")
    void getMyReviews_fail_invalidSort() throws Exception {
        // given
        setAuthPrincipal(1L);

        // when & then
        mockMvc.perform(get("/v1/reviews")
                        .param("sort", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 정렬 조건입니다."));
    }

//    @Test
//    @DisplayName("사용자별 리뷰 조회 - 실패 (비로그인 401)")
//    void getMyReviews_fail_unauthorized() throws Exception {
//        // given
//        SecurityContextHolder.clearContext();
//
//        // when & then
//        mockMvc.perform(get("/v1/reviews")
//                        .param("sort", "latest")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
//    }
}