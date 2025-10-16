package com.babgo.application.search;

import com.babgo.application.search.SearchInfo.CreateResult;
import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchService;
import com.babgo.domain.search.SearchType;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
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

        List<SearchCommand.CreateResult> searchList;

        // 검색 타입에 따라 분기
        switch (SearchType.valueOf(searchInfo.getSearchType().toUpperCase())) {
            case KATEGORIE -> searchList = searchService.getCategorySearch(searchInfo.toCommand());
            case STORE -> searchList = searchService.getStoreSearch(searchInfo.toCommand());
            default -> throw new CustomException(ErrorCode.BAD_REQUEST,"지원하지 않는 검색 타입입니다");
        }
        return CreateResult.from(searchList);
    }
}
