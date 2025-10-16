package com.babgo.repository.ai.config;

import com.babgo.domain.ai.by_search_recommendation.OpenAiProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/google-ai")
public class GoogleAiController {

    private final OpenAiProperties openAiProperties;
    private final RestClient restClient;

    public GoogleAiController(OpenAiProperties openAiProperties, RestClient.Builder builder) {
        this.openAiProperties = openAiProperties;
        String baseUrl = openAiProperties.getChat().getBaseUrl();
        String baseUrlWithSlash = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.restClient = builder.baseUrl(baseUrlWithSlash).build();
    }

    @GetMapping("/models")
    public Map<String, Object> getModels() {
        ResponseEntity<Map> response = restClient.get()
                .uri("v1beta/openai/models")
                .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                .retrieve()
                .toEntity(Map.class);
        return response.getBody();
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // 요청 메시지 구성
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", userMessage
        );

        // 페이로드 구성
        Map<String, Object> payload = Map.of(
                "model", "gemini-2.0-flash",
                "messages", List.of(message)
        );

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // RestTemplate로 POST 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions",
                entity,
                Map.class
        );

        // 실제 결과 Map 반환
        return response.getBody();
    }


    @PostMapping("/embeddings")
    public Map<String, Object> createEmbedding(@RequestBody Map<String, String> request) {
        String inputText = request.get("input");

        // 페이로드 구성
         Map<String, Object> payload = new HashMap<>();
         payload.put("model", "gemini-embedding-001");
         // 임베딩 모델
         payload.put("input", inputText);
        // 헤더 설정
         HttpHeaders headers = new HttpHeaders();
         headers.set("Authorization", "Bearer " + openAiProperties.getApiKey());
         headers.setContentType(MediaType.APPLICATION_JSON);
         HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
         // RestTemplate로 POST 요청
         RestTemplate restTemplate = new RestTemplate();
         ResponseEntity<Map> response = restTemplate.postForEntity( "https://generativelanguage.googleapis.com/v1beta/openai/embeddings", entity, Map.class );

         // 결과 반환
         return response.getBody();
    }
}

