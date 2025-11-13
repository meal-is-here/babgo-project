package com.babgo.domain.ai.store_summary;

import com.babgo.domain.ai.review_analysis.FakeReviewMLService;
import com.babgo.domain.ai.review_analysis.ReviewAnalysis;
import com.babgo.domain.store.Store;
import com.babgo.repository.ai.review_analysis.ReviewAnalysisRepositoryImpl;
import com.babgo.repository.ai.store_summary.StoreSummaryRepositoryImpl;
import com.babgo.repository.store.StoreRepositoryImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreSummaryService {

    private final StoreRepositoryImpl storeRepository;
    private final ReviewAnalysisRepositoryImpl reviewAnalysisRepository;
    private final StoreSummaryRepositoryImpl storeSummaryRepository;
    private final FakeReviewMLService fakeReviewMLService;

    @Value("${fastapi.REVIEW_ANALYSIS_URL}")
    private String fastApiBaseUrl;

    /**
     * WebClient 기반 비동기 요약 생성
     */
    @Transactional
    public Mono<String> generateSummaryReactive(UUID storeId) {
        // 1) 가게 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다: " + storeId));

        // 2) 리뷰 분석 조회
        List<ReviewAnalysis> analyses = reviewAnalysisRepository.findByReview_Store_StoreId(storeId);

        // 2-1) 리뷰 분석 자체가 없을 경우
        if (analyses.isEmpty()) {
            String msg = "해당 가게에 대한 리뷰 분석이 존재하지 않습니다.";
            return saveSummaryReactive(store, msg).thenReturn(msg);
        }

        // 3) 허위 가능성 높은 리뷰 제외
        List<ReviewAnalysis> valid = analyses.stream()
                .filter(a -> a.getFakeScore() < 0.5)
                .collect(Collectors.toList());

        if (valid.isEmpty()) {
            String msg = "유효한 리뷰가 충분하지 않아 요약을 생성할 수 없습니다.";
            return saveSummaryReactive(store, msg).thenReturn(msg);
        }

        // 4) 각 리뷰 허위 점수 ML 서버 호출 (Mono)
        Flux<Double> fakeScores = Flux.fromIterable(valid)
                .flatMap(a -> {
                    String content = a.getReview().getContent() == null ? "" : a.getReview().getContent();
                    return fakeReviewMLService.getFakeScoreFromML(content, fastApiBaseUrl)
                            .defaultIfEmpty(a.getFakeScore()); // ML 서버 실패 시 기존 값 fallback
                });

        return fakeScores.collectList()
                .map(scores -> {
                    // 평균 감정 점수
                    double avgSent = valid.stream().mapToDouble(ReviewAnalysis::getSentimentScore).average().orElse(0);

                    // 키워드 집계
                    Map<String, Long> kwCount = new HashMap<>();
                    for (ReviewAnalysis a : valid) {
                        String keywords = a.getKeywords();
                        if (keywords == null || keywords.isBlank()) continue;
                        for (String kw : keywords.split(",")) {
                            kwCount.put(kw, kwCount.getOrDefault(kw, 0L) + 1);
                        }
                    }

                    String topKeyword = kwCount.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("특색 없음");

                    String sentimentText = avgSent > 0 ? "긍정적인 리뷰가 많습니다." : "리뷰 평이 엇갈립니다.";

                    return String.format("'%s'은(는) '%s'을(를) 키워드로 한 리뷰가 많습니다. 또한 %s",
                            store.getStoreName(), topKeyword, sentimentText);
                })
                .flatMap(summary -> saveSummaryReactive(store, summary).thenReturn(summary));
    }

    /**
     * 요약 저장 (Mono)
     */
    private Mono<Void> saveSummaryReactive(Store store, String summaryText) {
        StoreSummary s = store.getStoreSummary();
        if (s == null) {
            s = new StoreSummary();
            s.setStore(store);
            store.setStoreSummary(s);
        }
        s.setSummaryText(summaryText);
        storeSummaryRepository.save(s); // JPA 동기 save
        return Mono.empty();
    }

}
