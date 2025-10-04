package com.babgo.controller.menu.dto;

import com.babgo.domain.menu.MenuStatus;
import lombok.Getter;

@Getter
public class MenuRequest {

    // 기본
    private String name;
    private Long price;
    private String description;
    private String category;
    private String createdBy;

    // 상태 변경 및 수정에 필요
    private MenuStatus status;
    private String updatedBy;
}
