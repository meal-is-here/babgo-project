//package com.babgo.domain.ai.by_search_recommendation;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class GoogleEmbeddingService {
//
//    private final RestClient restClient;
//
//    public GoogleEmbeddingService(RestClient.Builder builder) {
//        this.restClient = builder
//                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
//                .build();
//    }
//
//    public List<Double> embed(String text, String apiKey) {
//        // Google Embedding 요청 payload: text 배열로
//        Map<String, Object> payload = Map.of(
//                "text", List.of(text)
//        );
//
//        // 최신 엔드포인트: 모델을 path에 넣음
//        String modelEndpoint = "/models/text-embedding-3-large:embedText";
//
//        Map response = restClient.post()
//                .uri(modelEndpoint)
//                .header("Authorization", "Bearer " + apiKey)
//                .body(payload)   // bodyValue 사용
//                .retrieve()
//                .body(Map.class);
//
//        // response에서 embedding 추출
//        List<Map<String, Object>> embeddingsData = (List<Map<String, Object>>) response.get("embeddings");
//        Map<String, Object> firstEmbedding = embeddingsData.get(0);
//
//        return (List<Double>) firstEmbedding.get("embedding");
//    }
//}
