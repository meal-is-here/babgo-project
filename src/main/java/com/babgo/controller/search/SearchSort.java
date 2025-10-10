package com.babgo.controller.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchSort {

    ORDER_COUNT("주문많은순"),
    DISTANCE("거리순"),
    RATING("평점순"),
    LIKES("좋아요순"),
    CREATED("최신순");

    private final String description;

}
