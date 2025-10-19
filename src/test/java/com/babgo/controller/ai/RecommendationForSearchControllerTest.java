package com.babgo.controller.ai;

import com.babgo.domain.ai.by_search_recommendation.RecommendationForSearchService;
import com.babgo.domain.ai.by_search_recommendation.RecommendedStoreDTO;
import com.babgo.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = RecommendationForSearchController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
                org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration.class,
        }
)
class RecommendationForSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private RecommendationForSearchService recommendationService;

    @BeforeEach
    void setUp() {
        // Mockito 설정은 각 테스트에서 해줄 수 있음
    }

    @Test
    void 추천_요청시_정상응답_테스트() throws Exception {
        String query = "커피";
        List<RecommendedStoreDTO> stores = List.of(
                new RecommendedStoreDTO(UUID.randomUUID(), "스타벅스", "카페", "인기 메뉴 기반 추천"),
                new RecommendedStoreDTO(UUID.randomUUID(), "투썸플레이스", "카페", "최근 검색 키워드 기반 추천")
        );

        Mockito.when(recommendationService.recommendStoresWithReason(query, 5))
                .thenReturn(stores);

        mockMvc.perform(get("/v1/recommendations/ai")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].storeName").value("스타벅스"))
                .andExpect(jsonPath("$[0].categoryName").value("카페"))
                .andExpect(jsonPath("$[0].reason").value("인기 메뉴 기반 추천"))
                .andExpect(jsonPath("$[1].storeName").value("투썸플레이스"))
                .andExpect(jsonPath("$[1].categoryName").value("카페"))
                .andExpect(jsonPath("$[1].reason").value("최근 검색 키워드 기반 추천"));
    }

}
