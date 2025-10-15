package com.babgo.controller.menu.dto;

import com.babgo.domain.menu.MenuStatus;
import lombok.Getter;

@Getter
public class MenuRequest {
    private String name;
    private Long price;
    private String description;
    private String category;
    private String createdBy;

    // 상태변경 수정에 필요
    private MenuStatus status;
    private String updatedBy;

    // 추가
    private int stock; // 신규 등록 시 초기 재고
    private int quantity; // 재고 증가/감소 API에 사용
}

