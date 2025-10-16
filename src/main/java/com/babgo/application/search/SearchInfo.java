package com.babgo.application.search;

import com.babgo.domain.search.SearchCommand;
import com.babgo.domain.search.SearchSort;
import com.babgo.domain.search.SearchType;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import java.util.Arrays;
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

        private String regionCode;

        private String searchType;

        private String keyword;

        private String sort;

        private int page;

        private int size;

        private Create(double latitude, double longitude, String regionCode, String searchType, String keyword, String sort, int page, int size) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.regionCode = regionCode;
            this.searchType = searchType;
            this.keyword = keyword;
            this.sort = sort;
            this.page = page;
            this.size = size;
        }

        public static Create of(double latitude, double longitude, String regionCode, String searchType, String keyword, String sort, int page, int size) {
            return new Create(latitude, longitude, regionCode, searchType, keyword, sort, page, size);
        }

        public SearchCommand.Create toCommand(){

            return SearchCommand.Create.of(latitude, longitude, regionCode, validateSearchType(searchType), keyword, validateSort(sort), page, size);

        }


        private String validateSearchType(String type) {
            // 검색 타입이 없으면 에러
            if (type == null || type.isBlank()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "검색 타입이 비어있습니다.");
            }

            // 도메인 enum 기반 검증
            boolean isValid = Arrays.stream(SearchType.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(type));

            if (!isValid) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "유효하지 않은 검색 타입입니다: " + type);
            }

            return type.toUpperCase();
        }

        private String validateSort(String sort) {

            // null 이거나 공백이면 기본값: DISTANCE
            if (sort == null || sort.isBlank()) {
                return SearchSort.DISTANCE.name();
            }

            // SearchSort 안에 존재하는지 않아도 DISTANCE
            return Arrays.stream(SearchSort.values())
                .map(Enum::name)
                .anyMatch(s -> s.equalsIgnoreCase(sort)) ? sort.toUpperCase() : SearchSort.DISTANCE.name();
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

        private String regionCode;



        private CreateResult(UUID storeId, String storeName, UUID categoryId, String categoryName, double avgRating, int likes, String storeStatus, String regionCode) {
            this.storeId = storeId;
            this.storeName = storeName;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.avgRating = avgRating;
            this.likes = likes;
            this.storeStatus = storeStatus;
            this.regionCode = regionCode;
        }

        public static CreateResult of(UUID storeId, String storeName, UUID categoryId, String categoryName, double avgRating, int likes, String storeStatus, String regionCode) {
            return new CreateResult(storeId, storeName, categoryId, categoryName, avgRating, likes, storeStatus, regionCode);
        }


        public static List<CreateResult> from(List<SearchCommand.CreateResult> searchList){

            return searchList.stream()
                .map(search -> CreateResult.of(
                    search.getStoreId(),
                    search.getStoreName(),
                    search.getCategoryId(),
                    search.getCategoryName(),
                    search.getAvgRating(),
                    search.getLikes(),
                    search.getStoreStatus(),
                    search.getRegionCode()
                ))
                .toList();

        }



    }


}
