package com.babgo.domain.search;

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


    public List<SearchCommand.CreateResult> getStoreSearch(SearchCommand.Create searchCommand) {
        List<Search> list = searchRepository.getStoreSearch(searchCommand, DEFAULT_RADIUS_METER);
        return SearchCommand.CreateResult.from(list);
    }

    public List<SearchCommand.CreateResult> getCategorySearch(SearchCommand.Create searchCommand) {

        // 카테고리로 키로 레디스 있는지 확인
        List<SearchCache> searches = searchRedisRepository.getCategoryRegionCache(searchCommand, DEFAULT_RADIUS_METER);

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
    public void saveSearchWithNewTransaction(Search search) {
        searchRepository.saveSearch(search);
    }


    @Async("storeExecutor")
    public void saveCacheAsync(SearchCache cache) {
        try {
            searchRedisRepository.saveStoreCache(cache);
        } catch (Exception e) {
            log.error("Redis 캐시 저장 실패 (DB는 정상 반영됨): {}", e.getMessage());
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementOrderCountOnOrderTransaction(UUID storeId) {
        Search search = getSearchByStoreId(storeId);
        search.incrementOrderCount();

    }


    @Async("storeExecutor")
    public void incrementOrderCountCache(UUID storeId, UUID categoryId, String regionCode) {
        try {
            searchRedisRepository.incrementOrderCountCache(storeId, categoryId, regionCode);
        } catch (Exception e) {
            log.error("Redis 주문순 캐시 저장 실패 (DB는 정상 반영됨): {}", e.getMessage());
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementLikeCountOnUserTransaction(UUID storeId) {
        Search search = getSearchByStoreId(storeId);
        search.incrementLikeCount();

    }


    @Async("storeExecutor")
    public void incrementLikeCountCache(UUID storeId, UUID categoryId, String regionCode) {
        try {
            searchRedisRepository.incrementLikeCountCache(storeId, categoryId, regionCode);
        } catch (Exception e) {
            log.error("Redis 좋아요 캐시 저장 실패 (DB는 정상 반영됨): {}", e.getMessage());
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAverageRatingOnReviewrTransaction(UUID storeId, double averageRating) {
        Search search = getSearchByStoreId(storeId);
        search.updateAverageRating(averageRating);
    }


    @Async("storeExecutor")
    public void updateAverageRatingCache(UUID storeId, UUID categoryId, String regionCode, double averageRatinge) {
        try {
            searchRedisRepository.updateAverageRatingCache(storeId, categoryId, regionCode, averageRatinge);
        } catch (Exception e) {
            log.error("Redis 평점 캐시 저장 실패 (DB는 정상 반영됨): {}", e.getMessage());
        }
    }


    // 캐시가 없을 떄 등록
    @Async("storeExecutor")
    public void asyncRegisterCache(SearchCommand.Create searchCommand, List<Search> list) {

        SearchSort sort = SearchSort.valueOf(searchCommand.getSort().toUpperCase());
        for (Search search : list) {
            SearchCache cache = SearchCache.builder()
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

            searchRedisRepository.saveCacheBySort(cache,sort);
        }


    }

}
