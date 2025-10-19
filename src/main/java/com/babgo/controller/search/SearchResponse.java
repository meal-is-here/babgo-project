package com.babgo.controller.search;

import com.babgo.application.search.SearchInfo.CreateResult;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class SearchResponse {

    private UUID storeId;

    private String storeName;

    private UUID categoryId;

    private String categoryName;

    private double avgRating;

    private String storeStatus;


    public static SearchResponse from(CreateResult result) {
        SearchResponse response = new SearchResponse();
        response.storeId = result.getStoreId();
        response.storeName = result.getStoreName();
        response.categoryId = result.getCategoryId();
        response.categoryName = result.getCategoryName();
        response.avgRating = result.getAvgRating();
        response.storeStatus = result.getStoreStatus();
        return response;
    }


    public static List<SearchResponse> from(List<CreateResult> results) {
        return results.stream()
            .map(SearchResponse::from)
            .toList();
    }


}
