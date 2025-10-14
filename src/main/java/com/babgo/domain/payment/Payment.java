package com.babgo.domain.payment;

import com.babgo.global.entity.BaseTimeEntity;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

//TODO version 추가
@Getter
@Entity
@Table(name = "p_payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false, updatable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Version
    private Long version;
    //TODO 부분 유니크 인덱스
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "payment_amount", nullable = false, updatable=false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, updatable=false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_brand", updatable=false)
    private CardBrand cardBrand;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", updatable=false)
    private CardType cardType;

    private LocalDateTime approvedAt;

    private LocalDateTime refundedAt;

    private Payment(
            UUID orderId,
            Long userId,
            Long amount,
            PaymentMethod method,
            CardBrand cardBrand,
            CardType cardType
    ) {
        if (orderId == null){
            throw new CustomException(ErrorCode.INVALID, "주문 정보를 찾을 수 없습니다.");
        }
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage());
        }
        if (method == null) {
            throw new CustomException(ErrorCode.INVALID,"지원되지 않는 결제 유형입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID,"유효하지 않은 결제 금액입니다.");
        }
        if (method == PaymentMethod.CARD && cardBrand == null){
            throw new CustomException(ErrorCode.INVALID, "카드 결제 방식에는 카드 브랜드 정보가 필수 입니다.");
        }
        if (method == PaymentMethod.CARD && cardType == null) {
            throw new CustomException(ErrorCode.INVALID,"카드 결제 방식에는 카드 타입 정보가 필수입니다.");
        }

        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
        this.cardBrand = cardBrand;
        this.cardType = cardType;
        this.paymentStatus = PaymentStatus.READY;
    }

    public static Payment of(
            UUID orderId,
            Long userId,
            Long amount,
            PaymentMethod method,
            CardBrand cardBrand,
            CardType cardType
    ){
        return new Payment(
                orderId,
                userId,
                amount,
                method,
                cardBrand,
                cardType
        );
    }

    public void markApproved(String transactionId){
        this.paymentStatus = PaymentStatus.PAID;
        this.transactionId = transactionId;
    }

    public void markFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void markProcessing() {
        this.paymentStatus  = PaymentStatus.PROCESSING;
    }


}
