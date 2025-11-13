package com.babgo.controller.ai;

import com.babgo.domain.ai.by_search_recommendation.RecommendationForSearchService;
import com.babgo.domain.ai.by_search_recommendation.RecommendedStoreDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

class RecommendationForSearchControllerTest {

    private WebTestClient webTestClient;
    private RecommendationForSearchService recommendationService;

    @BeforeEach
    void setUp() {
        // Mockito로 서비스 Mock 생성
        recommendationService = Mockito.mock(RecommendationForSearchService.class);

        // 컨트롤러 생성 후 WebTestClient 바인딩
        RecommendationForSearchController controller =
                new RecommendationForSearchController(recommendationService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void 추천_요청시_정상응답_테스트() {
        String query = "커피";
        List<RecommendedStoreDTO> stores = List.of(
                new RecommendedStoreDTO(UUID.randomUUID(), "스타벅스", "카페", "인기 메뉴 기반 추천"),
                new RecommendedStoreDTO(UUID.randomUUID(), "투썸플레이스", "카페", "최근 검색 키워드 기반 추천")
        );

        // Mockito Stub
        Mockito.when(recommendationService.recommendStoresWithReason(query, 5))
                .thenReturn(stores);

        // WebTestClient로 요청
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/recommendations/ai")
                        .queryParam("query", query)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RecommendedStoreDTO.class)
                .value(response -> {
                    assert response.size() == 2;
                    assert response.get(0).getStoreName().equals("스타벅스");
                    assert response.get(0).getCategoryName().equals("카페");
                    assert response.get(0).getReason().equals("인기 메뉴 기반 추천");
                    assert response.get(1).getStoreName().equals("투썸플레이스");
                    assert response.get(1).getCategoryName().equals("카페");
                    assert response.get(1).getReason().equals("최근 검색 키워드 기반 추천");
                });
    }
}
