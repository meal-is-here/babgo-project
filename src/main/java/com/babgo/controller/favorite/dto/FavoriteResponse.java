package com.babgo.controller.favorite.dto;

import com.babgo.domain.favorite.Favorite;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class FavoriteResponse {

    private UUID favoriteId;

    private UUID menuId;

    private String menuName;

    private String storeName;

    private String option;

    private int quantity;

    private LocalDateTime createdAt;

    public FavoriteResponse(UUID favoriteId, UUID menuId, String menuName,
                            String storeName, String option, int quantity, LocalDateTime createdAt) {
        this.favoriteId = favoriteId;
        this.menuId = menuId;
        this.menuName = menuName;
        this.storeName = storeName;
        this.option = option;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getFavoriteId(),
                favorite.getMenu().getMenuId(),
                favorite.getMenu().getName(),
                favorite.getMenu().getStore().getStoreName(),
                favorite.getOption(),
                favorite.getQuantity(),
                favorite.getCreatedAt()
        );
    }
}
