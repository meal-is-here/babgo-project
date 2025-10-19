package com.babgo.controller.search;

import com.babgo.application.search.SearchFacade;
import com.babgo.application.search.SearchInfo.CreateResult;
import com.babgo.global.api.ApiResponse;
import com.babgo.global.security.annotation.CurrentUser;
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
    public ApiResponse<List<SearchResponse>> getSearch(
        @CurrentUser Long userId,
        @RequestParam  String searchType,
        @RequestParam  String keyword,
        @RequestParam  String sort,
        @RequestParam int page,
        @RequestParam int size
    ) {


        SearchRequest.Create request = SearchRequest.Create.of(userId, searchType, keyword, sort, page, size);

        List<CreateResult> result = searchFacade.getSearch(request.toSearchInfo());

        return ApiResponse.success("성공", SearchResponse.from(result));
    }

}
