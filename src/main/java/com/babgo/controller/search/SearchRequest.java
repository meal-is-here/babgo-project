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





        private Create(double latitude, double longitude, SearchType searchType, String keyword, SearchSort sort, int page, int size) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.searchType = searchType;
            this.keyword = keyword;
            this.sort = sort;
            this.page = page;
            this.size = size;
        }



        public static Create of (double latitude, double longitude, SearchType searchType, String keyword, SearchSort sort, int page, int size) {

            return new Create(latitude, longitude, searchType, keyword, sort, page, size);

        }

        public SearchInfo.Create toSearchInfo(){

            return SearchInfo.Create.of(latitude, longitude, searchType.name(), keyword, sort.name(), page, size);
        }


    }


}
