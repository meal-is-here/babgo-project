package com.babgo.domain.search;

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

        private String searchType;

        private String keyword;

        private String sort;

        private int page;

        private int size;

        private Create(double latitude, double longitude, String searchType, String keyword, String sort, int page, int size) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.searchType = searchType;
            this.keyword = keyword;
            this.sort = sort;
            this.page = page;
            this.size = size;
        }

        public static Create of(double latitude, double longitude, String searchType, String keyword, String sort, int page, int size) {
            return new Create(latitude, longitude, searchType, keyword, sort, page, size);
        }


    }

}
