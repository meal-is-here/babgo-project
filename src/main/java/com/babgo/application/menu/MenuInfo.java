package com.babgo.application.menu;

import com.babgo.domain.menu.Menu;
import com.babgo.domain.menu.MenuStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 조회 응답용 DTO (Builder 없이 생성자 방식)
 */
@Getter
public class MenuInfo {

    private UUID menuId;
    private UUID storeId;
    private String storeName;

    private String name;
    private Long price;
    private String description;
    private String category;

    private MenuStatus status;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;

    private int stock;

    // 생성자 방식
    public MenuInfo(UUID menuId, UUID storeId, String storeName, String name, Long price,
                    String description, String category, MenuStatus status,
                    int stock,
                    LocalDateTime createdAt, String createdBy,
                    LocalDateTime updatedAt, String updatedBy,
                    LocalDateTime deletedAt, String deletedBy) {
        this.menuId = menuId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
        this.stock = stock;
    }

    public static MenuInfo from(Menu menu) {
        if (menu == null) return null;

        UUID storeId = null;
        String storeName = null;
        if (menu.getStore() != null) {
            try {
                storeId = menu.getStore().getStoreId();
                storeName = menu.getStore().getStoreName();
            } catch (Exception ignored) {
            }
        }

        return new MenuInfo(
                menu.getMenuId(),
                storeId,
                storeName,
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getCategory(),
                menu.getMenuStatus(),
                menu.getStock(),
                menu.getCreatedAt(),
                menu.getCreatedBy(),
                menu.getUpdatedAt(),
                menu.getUpdatedBy(),
                menu.getDeletedAt(),
                menu.getDeletedBy()
        );
    }
}
