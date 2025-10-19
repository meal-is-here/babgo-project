package com.babgo.application.search;

import com.babgo.application.store.StoreCreatedEvent;
import com.babgo.application.store.StoreOrderCompletedEvent;
import com.babgo.domain.like.LikeChangedEvent;
import com.babgo.domain.review.ReviewChangedEvent;
import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchCache;
import com.babgo.domain.search.SearchService;
import com.babgo.domain.search.SearchSort;
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
    public void handleSearchCreate(StoreCreatedEvent event) {

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
            0,
            event.storeStatus(),
            event.latitude(),
            event.longitude()
        );

        // 레디스 등록
        SearchCache.Create cache = SearchCache.Create.builder()
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

        searchService.saveSearch(search);
        searchService.saveCacheAsync(cache);

    }


    // 주문완료 시 검색 데이터 비동기 반영 리스너
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlOrderCount(StoreOrderCompletedEvent event) {

        SearchCache.CountUpdate search = SearchCache.CountUpdate.builder()
            .storeId(event.storeId())
            .key(SearchCache.Key.builder().categoryId(event.categoryId().toString())
                .regionCode(event.regionCode()).sort(
                    SearchSort.ORDER_COUNT).build())
            .build();

        // 주문 했을때 db 저장
        searchService.incrementOrderCount(search);

        searchService.incrementOrderCountCache(search);

    }


    // 사용자 좋아요 검색 데이터 비동기 반영 리스너
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeCount(LikeChangedEvent event) {

        SearchCache.CountUpdate searchCache = SearchCache.CountUpdate.builder()
            .storeId(event.storeId())
            .actionType(event.action())
            .build();

        // 좋아요 했을때 db 저장
        searchService.incrementLikeCount(searchCache);

        searchService.incrementLikeCountCache(searchCache);

    }

    // 가게 평균 변경 시 검색 데이터 비동기 반영 리스너
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAvergeRatingChange(ReviewChangedEvent event) {

        SearchCache.Update search = SearchCache.Update.builder()
            .storeId(event.storeId())
            .newRating(event.newRating())
            .oldRating(event.oldRating())
            .actionType(event.action())
            .build();

        searchService.averageRatingChange(search);

        searchService.averageRatingCangeCache(search);

    }


}
