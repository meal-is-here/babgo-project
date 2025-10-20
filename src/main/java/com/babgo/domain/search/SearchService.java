package com.babgo.domain.search;

import com.babgo.domain.search.SearchCache.Update;
import com.babgo.domain.store.Store;
import com.babgo.domain.store.StoreRepository;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    // 검색 거리 반경
    public static final double DEFAULT_RADIUS_METER = 2000.0;

    private final SearchRepository searchRepository;

    private final SearchRedisRepository searchRedisRepository;

    private final StoreRepository storeRepository;


    public List<SearchCommand.CreateResult> getStoreSearch(SearchCommand.Create searchCommand) {
        List<Search> list = searchRepository.getStoreSearch(searchCommand, DEFAULT_RADIUS_METER);
        return SearchCommand.CreateResult.from(list);
    }

    public List<SearchCommand.CreateResult> getCategorySearch(SearchCommand.Create searchCommand) {

        // 카테고리로 키로 레디스 있는지 확인
        List<SearchCache.Result> searches = searchRedisRepository.getCacheByCategory(searchCommand, DEFAULT_RADIUS_METER);

        if (searches.isEmpty()) {
            List<Search> list = searchRepository.getCategorySearch(searchCommand, DEFAULT_RADIUS_METER);

            // 비동기 처리
            asyncRegisterCache(searchCommand, list);

            return SearchCommand.CreateResult.from(list);
        }

        return SearchCommand.CreateResult.fromCacheList(searches);

    }

    public Search getSearchByStoreId(UUID storeId) {
        return searchRepository.findByStoreId(storeId);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSearch(Search search) {
        searchRepository.saveSearch(search);
    }


    @Async("storeExecutor")
    public void saveCacheAsync(SearchCache.Create cache) {
        try {
            searchRedisRepository.saveStoreCache(cache);
        } catch (Exception e) {
            log.error("Redis 캐시 저장 실패 (DB는 정상 반영됨): {}", e.getMessage());
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementOrderCount(UUID storeId) {

        Store store = storeRepository.findByStoreId(storeId)
            .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        SearchCache.CountUpdate countUpdate = SearchCache.CountUpdate.builder()
            .storeId(store.getStoreId())
            .key(SearchCache.Key.builder().categoryId(store.getCategory().getCategoryId().toString())
                .regionCode(store.getRegionCode()).sort(
                    SearchSort.ORDER_COUNT).build())
            .build();

        Search search = getSearchByStoreId(countUpdate.getStoreId());
        search.incrementOrderCount();

    }


    @Async("storeExecutor")
    public void incrementOrderCountCache(UUID storeId) {
        try {

            Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

            SearchCache.CountUpdate countUpdate = SearchCache.CountUpdate.builder()
                .storeId(store.getStoreId())
                .key(SearchCache.Key.builder().categoryId(store.getCategory().getCategoryId().toString())
                    .regionCode(store.getRegionCode()).sort(
                        SearchSort.ORDER_COUNT).build())
                .build();


            searchRedisRepository.incrementOrderCountCache(countUpdate, SearchSort.ORDER_COUNT);
        } catch (Exception e) {
            log.error("Redis 주문순 캐시 저장 실패 (DB는 정상 반영됨): {}", e.getMessage());
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementLikeCount(SearchCache.CountUpdate countUpdate) {
        Search search = getSearchByStoreId(countUpdate.getStoreId());

        switch (countUpdate.getActionType()) {
            case CREATE -> search.incrementLikeCount();
            case CANCEL -> search.decrementLikeCount();
        }

    }


    @Async("storeExecutor")
    public void incrementLikeCountCache(SearchCache.CountUpdate countUpdate) {
        try {
            Search search = getSearchByStoreId(countUpdate.getStoreId());
            SearchCache.CountUpdate cache = countUpdate.toBuilder()
                .key(SearchCache.Key.builder()
                    .categoryId(search.getCategoryId().toString())
                    .regionCode(search.getRegionCode())
                    .sort(SearchSort.RATING).build())
                .build();
            searchRedisRepository.changeLikeCountCache(cache, SearchSort.LIKES);
        } catch (Exception e) {
            log.error("Redis 좋아요 캐시 저장 실패 (DB는 정상 반영됨): {}", e.getMessage());
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void averageRatingChange(Update update) {
        Search search = getSearchByStoreId(update.getStoreId());

        switch (update.getActionType()) {
            case CREATE -> search.createAverageRating(update.getNewRating());
            case UPDATE -> search.updateAverageRating(update.getNewRating());
            case DELETE -> search.deleteAverageRating(update.getOldRating());
        }

    }

    @Async("storeExecutor")
    public void averageRatingCangeCache(Update update) {

        Search search = getSearchByStoreId(update.getStoreId());

        switch (update.getActionType()) {
            case CREATE -> search.createAverageRating(update.getNewRating());
            case UPDATE -> search.updateAverageRating(update.getNewRating());
            case DELETE -> search.deleteAverageRating(update.getOldRating());
        }

        SearchCache.Update cache = update.toBuilder()
            .key(SearchCache.Key.builder()
                .categoryId(search.getCategoryId().toString())
                .regionCode(search.getRegionCode())
                .sort(SearchSort.RATING).build())
            .build();

        searchRedisRepository.changeAverageRatingCache(cache, SearchSort.RATING);

    }


    // 캐시가 없을 떄 등록
    @Async("storeExecutor")
    public void asyncRegisterCache(SearchCommand.Create searchCommand, List<Search> list) {

        SearchSort sort = searchCommand.getSort();

        for (Search search : list) {
            SearchCache.Create cache = SearchCache.Create.builder()
                .storeId(search.getStoreId())
                .storeName(search.getStoreName())
                .categoryId(search.getCategoryId())
                .categoryName(search.getCategoryName())
                .avgRating(search.getAvgRating())
                .likes(search.getLikes())
                .storeStatus(search.getStoreStatus())
                .regionCode(search.getRegionCode())
                .latitude(search.getLatitude())
                .longitude(search.getLongitude())
                .orderCount(search.getOrderCount())
                .createdAt(search.getCreatedAt())
                .build();

            searchRedisRepository.saveCacheBySort(cache, sort);
        }


    }

}
