package com.babgo.domain.search;

import java.util.List;

public interface SearchRedisRepository {


    List<SearchCache.Result> getCacheByCategory(SearchCommand.Create searchCommand, double radiusMeters);

    void saveStoreCache(SearchCache.Create cache);

    void incrementOrderCountCache(SearchCache.CountUpdate cache, SearchSort sort);

    void changeLikeCountCache(SearchCache.CountUpdate cache, SearchSort sort);

    void changeAverageRatingCache(SearchCache.Update cache, SearchSort sort);

    void saveCacheBySort(SearchCache.Create cache, SearchSort sort);
}
