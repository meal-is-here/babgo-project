package com.babgo.domain.search;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    // 검색 거리 반경
    public static final double DEFAULT_RADIUS_METER = 2000.0;

    private final SearchRepository searchRepository;


    public List<Search> getStoreSearch(SearchCommand.Create searchCommand) {
        return searchRepository.getStoreSearch(searchCommand, DEFAULT_RADIUS_METER);
    }

    public List<Search> getCategorySearch(SearchCommand.Create searchCommand) {
        return searchRepository.getCategorySearch(searchCommand, DEFAULT_RADIUS_METER);
    }
}
