package com.babgo.controller.menu.dto;

import lombok.Getter;

@Getter
public class MenuRequest {
    private String name;
    private Long price;
    private String description;
    private String category;
    private String createdBy;
}
