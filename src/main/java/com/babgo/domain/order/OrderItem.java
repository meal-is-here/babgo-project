package com.babgo.domain.order;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id", nullable = false, updatable = false)
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "menu_id", nullable = false)
    private UUID menuId;

    @Column(name = "menu_option_id", nullable = false)
    private UUID menuOptionId;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    private OrderItem(
            UUID orderItemId,
            Order order,
            UUID menuId,
            UUID menuOptionId,
            Long unitPrice,
            Long totalPrice,
            Integer quantity
    ) {

        if(unitPrice == null || unitPrice < 0){
            throw new CustomException(ErrorCode.INVALID,"주문 시점 메뉴의 가격은 0원 이상이어야합니다.");
        }

        if(quantity == null || quantity < 0){
            throw new CustomException(ErrorCode.INVALID,"주문 메뉴의 수량은 1개 이상이어야합니다.");
        }

        if (menuId == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR,"주문 정보를 찾을 수 없습니다.");
        }

        if (menuOptionId == null) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR,"메뉴의 옵션 정보를 찾을 수 없습니다.");
        }

        this.orderItemId = orderItemId;
        this.order = order;
        this.menuId = menuId;
        this.menuOptionId = menuOptionId;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
    }

    public static OrderItem of(
            UUID orderItemId,
            Order order,
            UUID menuId,
            UUID menuOptionId,
            Long unitPrice,
            Long totalPrice,
            Integer quantity
    ) {
        return new OrderItem(orderItemId, order, menuId, menuOptionId, unitPrice, totalPrice , quantity);
    }
}
