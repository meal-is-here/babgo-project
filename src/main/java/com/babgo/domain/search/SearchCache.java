package com.babgo.domain.search;

import com.babgo.domain.common.ActionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class SearchCache {


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create{
        private UUID storeId;

        private String storeName;

        private UUID categoryId;

        private String categoryName;

        private double avgRating;

        private int likes;

        private String storeStatus;

        private String regionCode;

        private double latitude;

        private double longitude;

        private int orderCount;

        private int reviewCount;

        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key {
        private String regionCode;
        private String categoryId;
        private SearchSort sort;
    }

    @Getter
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountUpdate {
        private UUID storeId;
        private UUID categoryId;
        private SearchSort sort;
        private ActionType actionType;
        private Key key;
    }

    @Getter
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private UUID storeId;
        private int newRating;
        private int oldRating;
        private ActionType actionType;

        private Key key;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private UUID storeId;

        private String storeName;

        private UUID categoryId;

        private String categoryName;

        private double avgRating;

        private String storeStatus;

        private String regionCode;

        private double latitude;

        private double longitude;

        private int orderCount;

        private int likes;

    }

}
