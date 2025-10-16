package com.babgo.controller.search;

import com.babgo.application.search.SearchFacade;
import com.babgo.application.search.SearchInfo.CreateResult;
import com.babgo.global.api.ApiResponse;
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
    public ApiResponse<List<SearchResponse>> getSearch( @RequestParam double latitude,
        @RequestParam  double longitude,
        @RequestParam  String searchType,
        @RequestParam String regionCode,
        @RequestParam  String keyword,
        @RequestParam  String sort,
        @RequestParam int page,
        @RequestParam int size
    ) {


        SearchRequest.Create request = SearchRequest.Create.of(latitude, longitude, regionCode, searchType, keyword, sort, page, size);

        List<CreateResult> result = searchFacade.getSearch(request.toSearchInfo());

        return ApiResponse.success("성공", SearchResponse.from(result));
    }

}
