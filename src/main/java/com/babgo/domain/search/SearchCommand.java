package com.babgo.domain.search;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchCommand {


    @Getter
    @NoArgsConstructor
    public static class  Create{

        private double latitude;

        private double longitude;

        private String regionCode;

        private String searchType;

        private String keyword;

        private SearchSort sort;

        private int page;

        private int size;

        private Create(double latitude, double longitude, String regionCode, String searchType, String keyword, String sort, int page, int size) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.regionCode = regionCode;
            this.searchType = searchType;
            this.keyword = keyword;
            this.sort = SearchSort.from(sort);
            this.page = page;
            this.size = size;
        }

        public static Create of(double latitude, double longitude, String regionCode, String searchType, String keyword, String sort, int page, int size) {
            return new Create(latitude, longitude, regionCode, searchType, keyword, sort, page, size);
        }


    }



    @Getter
    @NoArgsConstructor
    public static class CreateResult {

        private UUID storeId;

        private String storeName;

        private UUID categoryId;

        private String categoryName;

        private double avgRating;

        private String storeStatus;

        private String regionCode;

        private double latitude;

        private double longitude;



        public CreateResult(UUID storeId, String storeName, UUID categoryId, String categoryName, double avgRating,  String storeStatus, String regionCode, double latitude, double longitude) {
            this.storeId = storeId;
            this.storeName = storeName;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.avgRating = avgRating;
            this.storeStatus = storeStatus;
            this.regionCode = regionCode;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public static SearchCommand.CreateResult of(UUID storeId, String storeName, UUID categoryId, String categoryName, double avgRating, String storeStatus, String regionCode, double latitude, double longitude) {
            return new SearchCommand.CreateResult(storeId, storeName, categoryId, categoryName, avgRating, storeStatus, regionCode, latitude, longitude
            );
        }

        public static List<SearchCommand.CreateResult> from(List<Search> searchList) {
            return searchList.stream()
                .map(search -> SearchCommand.CreateResult.of(
                    search.getStoreId(),
                    search.getStoreName(),
                    search.getCategoryId(),
                    search.getCategoryName(),
                    search.getAvgRating(),
                    search.getStoreStatus(),
                    search.getRegionCode(),
                    search.getLatitude(),
                    search.getLongitude()
                ))
                .toList();
        }

        public static List<SearchCommand.CreateResult> fromCacheList(List<SearchCache.Result> searchList) {
            return searchList.stream()
                .map(search -> SearchCommand.CreateResult.of(
                    search.getStoreId(),
                    search.getStoreName(),
                    search.getCategoryId(),
                    search.getCategoryName(),
                    search.getAvgRating(),
                    search.getStoreStatus(),
                    search.getRegionCode(),
                    search.getLatitude(),
                    search.getLongitude()
                ))
                .toList();
        }
    }

}

