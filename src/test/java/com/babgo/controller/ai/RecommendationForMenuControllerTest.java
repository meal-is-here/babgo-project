package com.babgo.controller.ai;

import com.babgo.domain.ai.by_menu_recommendation.RecommendationForMenuService;
import com.babgo.domain.ai.by_menu_recommendation.RecommendedMenuDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

class RecommendationForMenuControllerTest {

    private WebTestClient webTestClient;
    private RecommendationForMenuService recommendationForMenuService;

    @BeforeEach
    void setUp() {
        recommendationForMenuService = Mockito.mock(RecommendationForMenuService.class);
        RecommendationForMenuController controller =
                new RecommendationForMenuController(recommendationForMenuService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void 메뉴_추천_정상응답_테스트() {
        // given
        Long userId = 1L;
        UUID storeId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String query = "커피";  // ✅ 추가됨
        int topK = 5;

        List<RecommendedMenuDTO> menus = List.of(
                new RecommendedMenuDTO("1", "아메리카노", "커피", "최근 인기 메뉴"),
                new RecommendedMenuDTO("2", "카페라떼", "커피", "사용자 선호 기반")
        );

        // when
        Mockito.when(recommendationForMenuService.recommendMenus(userId, storeId, query, topK))
                .thenReturn(menus);

        // then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/recommendations/menu")
                        .queryParam("userId", userId)
                        .queryParam("storeId", storeId)
                        .queryParam("query", query)  // ✅ 컨트롤러와 일치
                        .queryParam("topK", topK)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RecommendedMenuDTO.class)
                .value(response -> {
                    assert response.size() == 2;
                    assert response.get(0).menuName().equals("아메리카노");
                    assert response.get(1).menuName().equals("카페라떼");
                });
    }
}
