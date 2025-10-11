package com.babgo.controller.search;

import com.babgo.application.search.SearchInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchRequest {


    @Getter
    @NoArgsConstructor
    public static class Create{

        private double latitude;

        private double longitude;

        private SearchType searchType;

        private String keyword;

        private SearchSort sort;

        private int page;

        private int size;


        private Create(double latitude, double longitude, SearchType searchType, String keyword, SearchSort sort, Integer page, Integer size) {

           // 위도 검증
            if (latitude < -90.0 || latitude > 90.0) {
                throw new IllegalArgumentException("위도는 -90 이상 90 이하이어야 합니다.");
            }
            this.latitude = latitude;

            // 경도 검증
            if (longitude < -180.0 || longitude > 180.0) {
                throw new IllegalArgumentException("경도는 -180 이상 180 이하이어야 합니다.");
            }
            this.longitude = longitude;

            // 검색 타입 검증
            if (searchType == null) {
                throw new IllegalArgumentException("검색 타입이 없습니다.");
            }
            this.searchType = searchType;

            // 키워드 검증
            if (keyword == null || keyword.isBlank()) {
                throw new IllegalArgumentException("키워드를 입력 해주세요.");
            }
            this.keyword = keyword;

            // 정렬 기본값 처리
            this.sort = (sort != null) ? sort : SearchSort.CREATED;

            // 페이지 기본값 + 검증
            this.page = (page != null) ? page : 0;
            if (this.page < 0) {
                throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
            }

            // size 기본값 + 검증
            this.size = (size != null) ? size : 10;
            if (this.size <= 0) {
                throw new IllegalArgumentException("size는 1 이상이어야 합니다.");
            }
        }

        public static Create of(double latitude, double longitude, SearchType searchType, String keyword, SearchSort sort, Integer page, Integer size) {
            return new Create(latitude, longitude, searchType, keyword, sort, page, size);
        }

        public SearchInfo.Create toSearchInfo(){

            return SearchInfo.Create.of(latitude, longitude, searchType, keyword, sort.name(), page, size);
        }


    }


}
