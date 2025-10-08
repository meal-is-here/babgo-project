package com.babgo.application.search;

import com.babgo.domain.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchFasade {

    private final SearchService searchService;

    @Transactional(readOnly = true)
    public void getSearch(SearchInfo.Create searchInfo) {

        searchService.getSearch(searchInfo.toCommand());

    }
}
