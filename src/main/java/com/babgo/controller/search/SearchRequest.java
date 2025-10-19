package com.babgo.controller.search;

import com.babgo.application.search.SearchInfo;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchRequest {


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


        private Create(double latitude, double longitude, String regionCode, String searchType, String keyword, String sort, Integer page, Integer size) {

           // 위도 검증
            if (latitude < -90.0 || latitude > 90.0) {
                throw new  CustomException(ErrorCode.BAD_REQUEST, "위도는 -90 이상 90 이하이어야 합니다.");
            }
            this.latitude = latitude;

            // 경도 검증
            if (longitude < -180.0 || longitude > 180.0) {
                new CustomException(ErrorCode.BAD_REQUEST, "경도는 -180 이상 180 이하이어야 합니다.");
            }
            this.longitude = longitude;


            // 위치코드
            if(regionCode == null || regionCode.isEmpty()){
                throw new  CustomException(ErrorCode.BAD_REQUEST, "위치코드가 없습니다.");
            }
            this.regionCode = regionCode;

            // 검색 타입 검증
            this.searchType = searchType;

            // 키워드 검증
            if (keyword == null || keyword.isBlank()) {
                throw new  CustomException(ErrorCode.BAD_REQUEST, "키워드를 입력 해주세요.");
            }
            this.keyword = keyword;

            // 정렬 기본값 처리
            this.sort = sort;

            // 페이지 기본값 + 검증
            this.page = (page != null && page > 0) ? page : 1;

            // size 기본값 + 검증
            this.size = (size != null && size > 10)  ? size : 10;

        }

        public static Create of(double latitude, double longitude, String regionCode, String searchType, String keyword, String sort, Integer page, Integer size) {
            return new Create(latitude, longitude, regionCode, searchType, keyword, sort, page, size);
        }

        public SearchInfo.Create toSearchInfo(){

            return SearchInfo.Create.of(latitude, longitude, regionCode, searchType, keyword, sort, page, size);
        }


    }


}
