package com.babgo.domain.search;

import com.babgo.domain.search.SearchCommand.Create;
import java.util.List;

public interface SearchRepository {

    List<Search> getStoreSearch(Create searchCommand, double radiusMeters);

    List<Search> getCategorySearch(Create searchCommand, double radiusMeters);

    void saveSearch(Search search);
}
