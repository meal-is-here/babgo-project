package com.babgo.controller.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchSort {

    ORDER_COUNT("주문많은순"), // 주문많은순
    DISTANCE("거리순"),    // 거리순
    RATING("평점순"),      // 평점순
    LIKES("좋아요순"),
    ;

    private final String description;

}
