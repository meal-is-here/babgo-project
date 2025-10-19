package com.babgo.controller.favorite.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class FavoriteCreateRequest {

    @NotNull(message = "메뉴 ID는 필수값입니다.")
    private UUID menuId;

    @NotBlank(message = "옵션 정보는 필수값입니다.")
    private String option;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private int quantity;
}
