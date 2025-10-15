package com.babgo.controller.menu.dto;

import com.babgo.domain.menu.MenuStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MenuResponse {
    private UUID menuId;
    private String name;
    private Long price;
    private String description;
    private String category;
    private MenuStatus menuStatus;
    private int stock; // 재고 수량 추가
}
