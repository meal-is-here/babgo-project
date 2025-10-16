package com.babgo.domain.order;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemSnapshot {

    private UUID menuId;
    private String menuName;
    private long unitPrice;
    private int quantity;

    private OrderItemSnapshot(UUID menuId, String menuName, long unitPrice, int quantity) {
        if (menuId == null) throw new IllegalArgumentException("menuId must not be null");
        if (menuName == null || menuName.isBlank()) throw new IllegalArgumentException("menuName must not be blank");
        if (unitPrice < 0) throw new IllegalArgumentException("unitPrice must be >= 0");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        this.menuId = menuId;
        this.menuName = menuName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static OrderItemSnapshot of(UUID menuId, String menuName, long unitPrice, int quantity) {
        return new OrderItemSnapshot(menuId, menuName, unitPrice, quantity);
    }

    public long lineTotal() {
        return unitPrice * quantity;
    }

}
