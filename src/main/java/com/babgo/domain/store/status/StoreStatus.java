package com.babgo.domain.store.status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreStatus {

    OPEN("영업중"),
    CLOSED("영업 종료"),
    PREPARING("준비중");

    private final String text;

}
