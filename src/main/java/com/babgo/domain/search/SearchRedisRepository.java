package com.babgo.domain.search;

import java.util.List;

public interface SearchRedisRepository {


    List<SearchCache> getCategoryRegionCache(SearchCommand.Create searchCommand, double radiusMeters);

    void saveStoreCache(SearchCache cache);

}
