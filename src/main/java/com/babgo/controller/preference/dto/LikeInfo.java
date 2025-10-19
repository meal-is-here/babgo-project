package com.babgo.controller.preference.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LikeInfo {

    private UUID storeId;

    private String storeName;
}
