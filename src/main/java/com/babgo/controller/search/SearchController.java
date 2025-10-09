package com.babgo.controller.search;

import com.babgo.application.search.SearchFacade;
import com.babgo.application.search.SearchInfo.CreateResult;
import com.babgo.global.api.ApiResponse;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/search")
public class SearchController {

    private final SearchFacade searchFacade;

    @GetMapping("/stores")
    public ApiResponse<List<SearchResponse>> getSearch( @RequestParam @NotNull double latitude,
        @RequestParam @NotNull double longitude,
        @RequestParam @NotNull SearchType searchType,
        @RequestParam @NotNull String keyword,
        @RequestParam @NotNull SearchSort sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {


        SearchRequest.Create searchRequest = SearchRequest.Create.of(latitude, longitude, searchType, keyword, sort, page, size);

        List<CreateResult> result = searchFacade.getSearch(searchRequest.toSearchInfo());

        return ApiResponse.success("성공", SearchResponse.from(result));
    }

}
