package com.babgo.domain.search;

import java.util.List;
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
            return SearchCommand.CreateResult.from(list);
        }

        return SearchCommand.CreateResult.fromCacheList(searches);

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
}
