package com.babgo.application.search;

import com.babgo.application.store.StoreCreatedEvent;
import com.babgo.application.store.StoreOrderCompletedEvent;
import com.babgo.application.store.StoreRatingUpdatedEvent;
import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchCache;
import com.babgo.domain.search.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEventListener {


    private final SearchService searchService;

    // 가게 등록 시 검색 데이터 비동기 반영 리스너
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateSearchCacheOnStoreCreated(StoreCreatedEvent event) {

        log.info("StoreCreatedEvent 수신: {}", event.storeId());

        // 가게 생성 시점에는 평점, 좋아요, 주문수 초기 처리
        Search search = Search.of(
            event.storeId(),
            event.regionCode(),
            event.storeName(),
            event.categoryId(),
            event.categoryName(),
            0,
            0,
            0,
            event.storeStatus(),
            event.latitude(),
            event.longitude()
        );

        // 레디스 등록
        SearchCache cache = SearchCache.builder()
            .storeId(event.storeId())
            .storeName(event.storeName())
            .categoryId(event.categoryId())
            .categoryName(event.categoryName())
            .avgRating(0.0)       // 초기 평점
            .likes(0)             // 초기 좋아요 수
            .orderCount(0)        // 초기 주문 수
            .storeStatus(event.storeStatus())
            .regionCode(event.regionCode())
            .latitude(event.latitude())
            .longitude(event.longitude())
            .build();

        searchService.saveSearchWithNewTransaction(search);
        searchService.saveCacheAsync(cache);

    }


    // 주문완료 시 검색 데이터 비동기 반영 리스너
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void incrementOrderCountOnOrderCompleted(StoreOrderCompletedEvent event) {

        log.info("StoreCreatedEvent 수신: {}", event);

        // 주문 했을때 db 저장
        searchService.incrementOrderCountOnOrderTransaction(event.storeId());

        searchService.incrementOrderCountCache(event.storeId(), event.categoryId(), event.regionCode());

    }


    // 사용자 좋아요 검색 데이터 비동기 반영 리스너
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateLikeCountOnUserAction(StoreOrderCompletedEvent event) {

        log.info("StoreCreatedEvent 수신: {}", event);

        // 좋아요 했을때 db 저장
        searchService.incrementLikeCountOnUserTransaction(event.storeId());

        searchService.incrementLikeCountCache(event.storeId(), event.categoryId(), event.regionCode());

    }

    // 가게 평균 변경 시 검색 데이터 비동기 반영 리스너
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateAverageRatingOnReviewUpdated(StoreRatingUpdatedEvent event) {

        log.info("StoreCreatedEvent 수신: {}", event);

        // 평점 변경 했을때 db 저장
        searchService.updateAverageRatingOnReviewrTransaction(event.storeId(), event.averageRatinge());

        searchService.updateAverageRatingCache(event.storeId(), event.categoryId(), event.regionCode(), event.averageRatinge());

    }


}
