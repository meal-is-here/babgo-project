package com.babgo.domain.search;

import com.babgo.domain.search.SearchCommand.CreateResult;
import java.util.List;

public interface SearchRedisRepository {


    List<CreateResult> getCategoryRegionCache(SearchCommand.Create searchCommand);

}
