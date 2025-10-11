package com.babgo.domain.payment;

import com.babgo.global.entity.BaseTimeEntity;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

//TODO version 추가
@Entity
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false, updatable = false, unique = true)
    private UUID orderId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider",nullable = false, updatable=false)
    private PaymentProvider provider;
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
    @Column(name = "card_type", updatable=false)
    private CardType cardType;

    private OffsetDateTime approvedAt;

    @Column(name = "approve_no")
    private String approveNo;

    private OffsetDateTime refundedAt;

    private Payment(
            UUID orderId,
            UUID userId,
            PaymentProvider provider,
            Long amount,
            PaymentMethod method,
            CardType cardType
    ) {
        if (orderId == null){
            throw new CustomException(ErrorCode.INVALID, "주문 정보를 찾을 수 없습니다.");
        }
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getMessage());
        }
        if (provider == null){
            throw new CustomException(ErrorCode.INVALID, "올바르지 않은 pg 요청입니다.");
        }
        if (method == null) {
            throw new CustomException(ErrorCode.INVALID,"지원되지 않는 결제 유형입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID,"유효하지 않은 결제 금액입니다.");
        }
        if (method == PaymentMethod.CARD && cardType == null) {
            throw new CustomException(ErrorCode.INVALID,"카드 결제 방식에는 카드 타입 정보가 필수입니다.");
        }

        this.orderId = orderId;
        this.userId = userId;
        this.provider = provider;
        this.amount = amount;
        this.method = method;
        this.cardType = cardType;
        this.paymentStatus = PaymentStatus.READY;
    }

    public static Payment of(
            UUID orderId,
            UUID userId,
            PaymentProvider provider,
            Long amount,
            PaymentMethod method,
            CardType cardType
    ){
        return new Payment(
                orderId,
                userId,
                provider,
                amount,
                method,
                cardType
        );
    }

    //TODO paymentType 변동 메서드 추가 예정
}
