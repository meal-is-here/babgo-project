package com.babgo.domain.search;

import com.babgo.domain.search.SearchCommand.Create;
import java.util.List;

public interface SearchRepository {

    List<Search> getStores(Create searchCommand, double radiusMeters);

    List<Search> getCategoryStores(Create searchCommand, double radiusMeters);
}
