package com.babgo.controller.preference.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PreferenceResponse {

    private List<LikeInfo> likes;

    private List<FavoriteInfo> favorites;
}
