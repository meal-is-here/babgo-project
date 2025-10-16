package com.babgo.domain.search;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCache {

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

    private LocalDateTime createdAt;

}
