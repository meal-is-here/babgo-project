package com.babgo.domain.ai.by_search_recommendation;

import com.babgo.domain.ai.review_analysis.ReviewAnalysis;
import com.babgo.domain.store.Store;
import com.babgo.repository.ai.review_analysis.ReviewAnalysisRepositoryImpl;
import com.babgo.repository.store.StoreRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@EnableAsync
public class StoreSimilarityService {

    private final OpenAiProperties openAiProperties;
    private final ReviewAnalysisRepositoryImpl reviewAnalysisRepository;
    private final StoreRepositoryImpl storeRepository;

    @Value("${GOOGLE_AI_API_KEY}")
    private String googleApiKey;

    // 간단 캐시
    private final Map<String, List<Double>> embeddingCache = new HashMap<>();

    /**
     * 사용자 쿼리와 가게 리뷰 기반으로 유사도 계산 (비동기 + 캐싱)
     */
    public List<Store> findSimilarStores(String userQuery, int topK) {
        try {
            // 1️⃣ 사용자 임베딩 비동기 호출
            CompletableFuture<List<Double>> userVectorFuture = getEmbeddingAsync(userQuery);

            // 2️⃣ 전체 가게 후보 로드
            List<Store> candidates = storeRepository.findAll();

            // 3️⃣ 각 가게 임베딩 비동기 호출
            Map<Store, CompletableFuture<List<Double>>> storeEmbeddingFutures = new HashMap<>();
            for (Store store : candidates) {
                List<ReviewAnalysis> analyses = reviewAnalysisRepository.findByReview_Store_StoreId(store.getStoreId());
                if (analyses.isEmpty()) continue;

                String keywordsCombined = analyses.stream()
                        .map(ReviewAnalysis::getKeywords)
                        .collect(Collectors.joining(", "));

                storeEmbeddingFutures.put(store, getEmbeddingAsync(keywordsCombined));
            }

            // 4️⃣ 모든 Future 모으기
            CompletableFuture<?> allFutures = CompletableFuture.allOf(
                    storeEmbeddingFutures.values().toArray(new CompletableFuture[0])
            );

            // 5️⃣ 완료 대기
            allFutures.join();

            List<Double> userVector = userVectorFuture.get();
            if (userVector.isEmpty()) throw new RuntimeException("사용자 임베딩 생성 실패");

            // 6️⃣ 유사도 계산
            Map<Store, Double> similarityMap = new HashMap<>();
            for (Map.Entry<Store, CompletableFuture<List<Double>>> entry : storeEmbeddingFutures.entrySet()) {
                List<Double> storeVector = entry.getValue().get();
                if (storeVector.isEmpty()) continue;

                List<ReviewAnalysis> analyses = reviewAnalysisRepository.findByReview_Store_StoreId(entry.getKey().getStoreId());
                double avgSentiment = analyses.stream()
                        .mapToDouble(ReviewAnalysis::getSentimentScore)
                        .average().orElse(0.5);

                similarityMap.put(entry.getKey(), cosineSimilarity(userVector, storeVector) * avgSentiment);
            }

            // 7️⃣ 상위 K개 반환
            return similarityMap.entrySet().stream()
                    .sorted(Map.Entry.<Store, Double>comparingByValue().reversed())
                    .limit(topK)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("임베딩 처리 중 오류 발생", e);
        }
    }

    /**
     * 비동기 임베딩 생성 (캐시 포함)
     */
    @Async
    public CompletableFuture<List<Double>> getEmbeddingAsync(String inputText) {
        // 캐시 확인
        if (embeddingCache.containsKey(inputText)) {
            return CompletableFuture.completedFuture(embeddingCache.get(inputText));
        }

        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/projects/%s/locations/%s/models/gemini-embedding-001:embedText",
                    openAiProperties.getGoogleProjectId(),
                    openAiProperties.getGoogleLocation()
            );

            Map<String, Object> payload = Map.of("input", inputText);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + googleApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            Map<?, ?> body = response.getBody();
            if (body == null || !body.containsKey("responses")) {
                throw new IllegalStateException("임베딩 생성 실패: response 데이터 없음");
            }

            List<Double> embedding = ((List<?>) ((Map<?, ?>) ((List<?>) body.get("responses")).get(0)).get("embedding"))
                    .stream()
                    .map(v -> ((Number) v).doubleValue())
                    .collect(Collectors.toList());

            if (embedding.isEmpty()) {
                throw new IllegalStateException("임베딩 생성 실패: embedding 비어있음");
            }

            embeddingCache.put(inputText, embedding);

            return CompletableFuture.completedFuture(embedding);

        } catch (Exception e) {
            throw new RuntimeException("임베딩 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 코사인 유사도 계산
     */
    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.isEmpty() || v2.isEmpty() || v1.size() != v2.size()) return 0.0;

        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += v1.get(i) * v1.get(i);
            normB += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-9);
    }
}
