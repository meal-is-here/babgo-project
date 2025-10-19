package com.babgo.domain.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchType {

    KATEGORIE("카테고리"),
    STORE("가게"),
    ;

    private final String description;

}
