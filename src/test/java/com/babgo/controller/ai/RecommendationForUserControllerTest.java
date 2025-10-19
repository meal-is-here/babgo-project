package com.babgo.controller.ai;

import com.babgo.application.ai.RecommendationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

class RecommendationForUserControllerTest {

    private WebTestClient webTestClient;
    private RecommendationFacade recommendationFacade;

    @BeforeEach
    void setUp() {
        recommendationFacade = Mockito.mock(RecommendationFacade.class);
        RecommendationForUserController controller = new RecommendationForUserController(recommendationFacade);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void 추천_요청시_정상응답_테스트() {
        Long userId = 1L;
        List<RecommendationForUserResponse.StoreInfo> stores = List.of(
                new RecommendationForUserResponse.StoreInfo(UUID.randomUUID().toString(), "스타벅스"),
                new RecommendationForUserResponse.StoreInfo(UUID.randomUUID().toString(), "투썸플레이스")
        );
        RecommendationForUserResponse mockResponse = new RecommendationForUserResponse(stores);

        Mockito.when(recommendationFacade.getRecommendations(userId))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/v1/recommendations/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RecommendationForUserResponse.class)
                .value(response -> {
                    assert response.stores().size() == 2;
                    assert response.stores().get(0).storeName().equals("스타벅스");
                    assert response.stores().get(1).storeName().equals("투썸플레이스");
                });
    }
}
