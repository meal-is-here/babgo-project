package com.babgo.domain.ai.by_search_recommendation;

import com.babgo.domain.store.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final OpenAiProperties openAiProperties;
    private final AIRequestLogRepository aiRequestLogRepository; // 추가

    // 기존 가게 추천 함수
    public String generateRecommendationReason(String userQuery, Store store) {

        String prompt = String.format(
                "사용자가 '%s'라고 검색했을 때, 가게 '%s'를 추천하는 이유를 30~50자 내외로 자연스럽게 설명해줘. " +
                        "리뷰 감정과 키워드를 참고해서 작성.",
                userQuery, store.getStoreName()
        );

        return callAiApi(prompt);
    }

    // -------------------------------
    // 메뉴 추천 전용 함수
    // -------------------------------
    public String generateMenuRecommendationReason(String userQuery, String menuName, Store store) {

        String prompt = String.format(
                "사용자가 '%s'라고 검색했을 때, 가게 '%s'의 메뉴 '%s'를 추천하는 이유를 30~50자 내외로 자연스럽게 설명해줘. " +
                        "리뷰 감정과 키워드를 참고해서 작성.",
                userQuery, store.getStoreName(), menuName
        );

        return callAiApi(prompt);
    }

    // -------------------------------
    // 공통 AI 호출 로직
    // -------------------------------
    private String callAiApi(String prompt) {

        // 요청 payload
        Map<String, Object> requestPayload = Map.of(
                "model", openAiProperties.getChat().getModel(),   // ex: gemini-2.0-flash
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openAiProperties.getApiKey());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestPayload, headers);

        // RestTemplate로 POST 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
                requestEntity,
                Map.class
        );

//        // 응답에서 텍스트 추출
//        Map<String, Object> responseBody = response.getBody();
//        if (responseBody == null) return "";
//
//        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
//        if (choices != null && !choices.isEmpty()) {
//            Map<String, Object> firstChoice = choices.get(0);
//            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
//            return message != null ? (String) message.get("content") : "";
//        }
//
//        return "";
        Map<String, Object> responseBody = response.getBody();
        String aiText = "";

        if (responseBody != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                aiText = message != null ? (String) message.get("content") : "";
            }
        }

        // -------------------------------
        // ① AI 요청/응답 로그 저장
        // -------------------------------
        AIRequestLog log = AIRequestLog.builder()
                .prompt(prompt)
                .response(aiText)
                .createdAt(LocalDateTime.now())
                .build();

        aiRequestLogRepository.save(log);

        return aiText;
    }
}
