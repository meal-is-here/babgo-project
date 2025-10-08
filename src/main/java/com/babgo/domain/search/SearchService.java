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

    public List<Search> getSearch(SearchCommand.Create searchCommand) {
        // 검색 타입에 따른 분기 처리
        String searchType = searchCommand.getSearchType();
        
        return switch (searchType) {
            case "STORE" -> searchByStore(searchCommand);
            case "KATEGORIE" -> searchByCategory(searchCommand);
            default -> throw new IllegalArgumentException("지원하지 않는 검색 타입입니다: " + searchType);
        };
    }


    private List<Search> searchByStore(SearchCommand.Create searchCommand) {
        return searchRepository.getStores(searchCommand, DEFAULT_RADIUS_METER);
    }

    private List<Search> searchByCategory(SearchCommand.Create searchCommand) {
        return searchRepository.getCategoryStores(searchCommand, DEFAULT_RADIUS_METER);
    }
}
