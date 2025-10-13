package com.babgo.application.search;

import com.babgo.controller.search.SearchType;
import com.babgo.domain.search.Search;
import com.babgo.domain.search.SearchCommand;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchInfo {

    @Getter
    @NoArgsConstructor
    public static class Create{

        private double latitude;

        private double longitude;

        private SearchType searchType;

        private String keyword;

        private String sort;

        private int page;

        private int size;

        private Create(double latitude, double longitude, SearchType searchType, String keyword, String sort, int page, int size) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.searchType = searchType;
            this.keyword = keyword;
            this.sort = sort;
            this.page = page;
            this.size = size;
        }

        public static Create of(double latitude, double longitude, SearchType searchType, String keyword, String sort, int page, int size) {
            return new Create(latitude, longitude, searchType, keyword, sort, page, size);
        }

        public SearchCommand.Create toCommand(){

            return SearchCommand.Create.of(latitude, longitude, searchType.name(), keyword, sort, page, size);

        }


    }


    @Getter
    @NoArgsConstructor
    public static class CreateResult{

        private UUID storeId;

        private String storeName;

        private UUID categoryId;

        private String categoryName;

        private double avgRating;

        private int likes;

        private String storeStatus;


        private CreateResult(UUID storeId, String storeName, UUID categoryId, String categoryName, double avgRating, int likes, String storeStatus){
            this.storeId = storeId;
            this.storeName = storeName;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.avgRating = avgRating;
            this.likes = likes;
            this.storeStatus = storeStatus;
        }

        public static CreateResult of(UUID storeId, String storeName, UUID categoryId, String categoryName, double avgRating, int likes, String storeStatus) {
            return new CreateResult(storeId, storeName, categoryId, categoryName, avgRating, likes, storeStatus);
        }


        public static List<CreateResult> from(List<Search> searchList){

            return searchList.stream()
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

        }



    }


}
