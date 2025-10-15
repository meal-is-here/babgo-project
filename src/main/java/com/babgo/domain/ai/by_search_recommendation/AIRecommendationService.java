package com.babgo.domain.ai.by_search_recommendation;

import com.babgo.domain.store.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final OpenAiProperties openAiProperties;
    private final RestClient restClient;

    public String generateRecommendationReason(String userQuery, Store store) {

        String prompt = String.format(
                "사용자가 '%s'라고 검색했을 때, 가게 '%s'를 추천하는 이유를 30~50자 내외로 자연스럽게 설명해줘. " +
                        "리뷰 감정과 키워드를 참고해서 작성.",
                userQuery, store.getStoreName()
        );

        // 요청 payload
        Map<String, Object> request = Map.of(
                "model", openAiProperties.getChat().getModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        // API 호출
        ResponseEntity<Map> response = restClient.post()
                .uri(openAiProperties.getChat().getCompletionPath())
                .body(request)
                .retrieve()
                .toEntity(Map.class);

        // 응답에서 텍스트 추출
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            return message != null ? (String) message.get("content") : "";
        }
        return "";
    }
}
