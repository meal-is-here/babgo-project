package com.babgo.application.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FakeReviewMLService {

    private final WebClient webClient;

    // 비동기 요청
    public Mono<Double> getFakeScoreFromML(String content, String baseUrl) {
        String url = baseUrl + "/predict";

        return webClient.post()
                .uri(url)
                .bodyValue(Map.of("content",content))
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    Object score = res.get("fake_score");
                    if (score instanceof Number) {
                        return ((Number) score).doubleValue();
                    }
                    return null;
                })
                .onErrorResume(e -> {
                    System.out.println("ML server not available :" + e.getMessage());
                    return Mono.empty();
                });
    }
}
