package com.babgo.domain.search;

import java.util.List;
import java.util.UUID;

public interface SearchRedisRepository {


    List<SearchCache> getCategoryRegionCache(SearchCommand.Create searchCommand, double radiusMeters);

    void saveStoreCache(SearchCache cache);

    void incrementOrderCountCache(UUID storeId, UUID categoryId, String regionCode);

    void incrementLikeCountCache(UUID storeId, UUID categoryId, String regionCode);

    void updateAverageRatingCache(UUID storeId, UUID categoryId, String regionCode, double averageRatinge);

    void saveCacheBySort(SearchCache cache, SearchSort sort);
}
