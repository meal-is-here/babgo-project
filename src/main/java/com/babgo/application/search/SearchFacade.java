package com.babgo.application.search;

import com.babgo.application.search.SearchInfo.CreateResult;
import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchFacade {

    private final SearchService searchService;

    @Transactional(readOnly = true)
    public List<CreateResult> getSearch(SearchInfo.Create searchInfo) {

        List<Search> searchList = searchService.getSearch(searchInfo.toCommand());

        List<CreateResult> results = searchList.stream()
            .map(search -> CreateResult.of(
                search.getStoreId(),
                search.getStoreName(),
                search.getCategoryId(),
                search.getCategoryName(),
                search.getAvgRating(),
                search.getLikes(),
                search.getStoreStatus()
            ))
            .toList();

        return results;
    }
}
