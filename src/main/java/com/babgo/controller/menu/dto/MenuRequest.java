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

    private MenuStatus status;
    private String updatedBy;
}
