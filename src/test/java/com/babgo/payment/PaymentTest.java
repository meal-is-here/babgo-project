package com.babgo.payment;

import com.babgo.domain.payment.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Payment 엔티티 테스트")
public class PaymentTest {

    @Test
    @DisplayName("결제 생성 - 상태는 READY")
    void createPayment_shouldHaveReadyStatus() {

        Payment payment = Payment.of(
                UUID.randomUUID(),
                1L,
                10000L,
                PaymentMethod.CARD,
                CardBrand.HYUNDAI,
                CardType.CREDIT
        );

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.READY);
    }

    @Test
    @DisplayName("READY 상태를 PROCESSING으로 변경")
    void markProcessing() {
        Payment payment = Payment.of(
                UUID.randomUUID(), 1L, 10000L,
                PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CREDIT
        );

        payment.markProcessing();

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PROCESSING);
    }

    @Test
    @DisplayName("결제 승인 - transactionId 저장")
    void markApproved_savesTransactionId() {
        Payment payment = Payment.of(
                UUID.randomUUID(), 1L, 10000L,
                PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CREDIT
        );
        String transactionId = "TXN_12345";

        payment.markApproved(transactionId);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getTransactionId()).isEqualTo(transactionId);
    }

    @Test
    @DisplayName("결제 실패 처리")
    void markFailed() {
        Payment payment = Payment.of(
                UUID.randomUUID(), 1L, 10000L,
                PaymentMethod.CARD, CardBrand.HYUNDAI, CardType.CREDIT
        );

        payment.markFailed();

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}
