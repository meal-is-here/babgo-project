package com.babgo.domain.order;

import com.babgo.global.entity.BaseTimeEntity;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Version
    @Column(name = "order_version", nullable = false)
    private Long version;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    private String deliveryRequest;

    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Transient
    private List<OrderItemSnapshot> itemSnapshotList = Collections.emptyList();

    private Order(
            UUID orderId,
            UUID store,
            Long user,
            String deliveryRequest,
            String deliveryAddress,
            Long totalPrice
    ){

        if (orderId == null) {
            throw new CustomException(ErrorCode.INVALID, "주문 아이디가 올바르지 않습니다.");
        }

        if (totalPrice == null || totalPrice < 0) {
            throw new CustomException(ErrorCode.INVALID, "총 가격은 0원 이상이어야 합니다.");
        }

        if (user == null){
            throw new CustomException(ErrorCode.INVALID, "사용자의 정보가 올바르지 않습니다.");
        }

        if (store == null){
            throw new CustomException(ErrorCode.INVALID, "음식점의 정보가 올바르지 않습니다.");
        }

        if (deliveryAddress == null || deliveryAddress.isBlank()){
            throw new CustomException(ErrorCode.INVALID,  "주소는 반드시 입력되어야 합니다.");
        }

        this.orderId = orderId;
        this.storeId = store;
        this.userId = user;
        this.deliveryRequest = deliveryRequest;
        this.deliveryAddress = deliveryAddress;
        this.totalPrice = totalPrice;
        this.orderStatus = OrderStatus.PENDING;
    }

    public static Order of(
            UUID orderId,
            UUID store,
            Long user,
            String deliveryRequest,
            String deliveryAddress,
            Long totalPrice
    ){
        return new Order(orderId, store, user, deliveryRequest, deliveryAddress, totalPrice);
    }

    public void markConfirmed() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void markPaymentInProgress(){
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_IN_PROGRESS, "이미 결제가 진행 중이거나 완료된 주문입니다.");
        }

        this.orderStatus = OrderStatus.PAYMENT_IN_PROGRESS;}

    public void markCancel(){
        if (orderStatus != OrderStatus.PENDING) {
            throw new CustomException(ErrorCode.ORDER_NOT_CANCELABLE, "현재 상태에서는 취소할 수 없습니다.");
        }

        this.orderStatus = OrderStatus.CANCELED;
    }

    public boolean isCompleted() {
        return this.orderStatus == OrderStatus.CONFIRMED;
    }

    public void markExpired() {
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new CustomException(
                    ErrorCode.INVALID_ORDER_STATE,
                    "PENDING 상태만 만료 처리할 수 있습니다."
            );
        }
        this.orderStatus = OrderStatus.EXPIRED;
    }




}
