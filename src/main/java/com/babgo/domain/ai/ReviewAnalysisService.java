package com.babgo.domain.ai;

import com.babgo.domain.review.Review;
import com.babgo.domain.ai.review_analysis.ReviewAnalysis;
import com.babgo.repository.ai.review_analysis.ReviewAnalysisRepositoryImpl;
import com.babgo.repository.review.ReviewRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final ReviewRepositoryImpl reviewRepository;
    private final ReviewAnalysisRepositoryImpl reviewAnalysisRepository;
    private final FakeReviewMLService fakeReviewMLService;

    @Value("${fastapi.REVIEW_ANALYSIS_FASTAPI_URL}")
    private String fastApiBaseUrl;

    /**
     * 비동기 분석: API 응답 시간을 빠르게 하기 위해 @Async로 별도 스레드에서 실행
     */
    @Async
    @Transactional
    public void analyzeReviewAsync(UUID reviewId) {
        analyzeReviewReactive(reviewId)
                .doOnError(e -> System.out.println("리뷰 분석 중 오류 발생: " + e.getMessage()))
                .subscribe();  // ← 반드시 구독해야 실행됨
    }

    /**
     * Mono 기반 비동기 분석
     */
    @Transactional
    public Mono<Void> analyzeReviewReactive(UUID reviewId) {
        // 1) 리뷰 조회
        Review review = reviewRepository.findById(reviewId); // null일 수 있음
        if (review == null) {
            throw new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId);
        }

        String content = review.getContent() == null ? "" : review.getContent();

        // 2) 감점 점수 & 키워드 계산 (똥기)
        double sentimentScore = computeSentimentScore(content);
        String keywords = extractKeywords(content);

        // 3) ML 서버 호출
        return fakeReviewMLService.getFakeScoreFromML(content, fastApiBaseUrl)
                .defaultIfEmpty(computeFakeScoreRule(review)) // ML 서버 실패 시 룰 기반 fallback
                .map(fakeScore -> {
                    // 4) 분석 저장
                    ReviewAnalysis analysis = review.getAnalysis();
                    if (analysis == null) {
                        analysis = new ReviewAnalysis();
                        analysis.setReview(review);
                        review.setAnalysis(analysis); // 양방향 연관관계
                    }
                    analysis.setSentimentScore(sentimentScore);
                    analysis.setKeywords(keywords);
                    analysis.setFakeScore(fakeScore);

                    reviewAnalysisRepository.save(analysis); // JPA 동기 save
                    return analysis;
                })
                .then(); // Mono<void> 반환
    }

    // ------------------
    // 감정 점수 계산
    private double computeSentimentScore(String content) {
        double score = 0;
        String[] pos = {"맛있", "좋", "최고", "친절", "빠르"};
        String[] neg = {"별로", "맛없", "실망", "느리", "불친절"};

        for (String p : pos) if (content.contains(p)) score++;
        for (String n : neg) if (content.contains(n)) score--;
        return score;
    }

    // 키워드 추출
    private String extractKeywords(String content) {
        String[] tokens = Arrays.stream(content.split("\\s+"))
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
        int len = Math.min(3, tokens.length);
        if (len == 0) return "";
        return String.join(",", Arrays.copyOf(tokens, len));
    }

    // 룰 기반 Fake 점수
    private double computeFakeScoreRule(Review review) {
        String content = review.getContent() == null ? "" : review.getContent();
        int length = content.length();
        double score = 0.0;
        if (length < 5) score += 0.5;
        if (review.getRating() == 5 && content.contains("별로")) score += 0.7;
        if (content.contains("최고") || content.contains("강추")) score += 0.2;
        return Math.min(1.0, score);
    }
}
