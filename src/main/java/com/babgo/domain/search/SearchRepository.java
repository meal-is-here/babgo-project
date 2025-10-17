package com.babgo.domain.search;

import com.babgo.domain.search.SearchCommand.Create;
import java.util.List;
import java.util.UUID;

public interface SearchRepository {

    List<Search> getStoreSearch(Create searchCommand, double radiusMeters);

    List<Search> getCategorySearch(Create searchCommand, double radiusMeters);

    void saveSearch(Search search);

    Search findByStoreId(UUID storeId);
}
