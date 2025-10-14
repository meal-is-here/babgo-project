package com.babgo.controller.payment;

import com.babgo.domain.payment.CardBrand;
import com.babgo.domain.payment.CardType;
import com.babgo.domain.payment.PaymentMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
public class PaymentRequest {

    @Getter
    @RequiredArgsConstructor
    public static class Ready {
        private final UUID orderId;
        private final Long amount;
        private final PaymentMethod paymentType;
        private final CardBrand cardBrand;
        private final CardType cardType;
    }

    @Getter
    @RequiredArgsConstructor
    public static class WebhookPayload {
        private final String event;
        private final String orderId;
        private final String paymentKey;
        private final String transactionKey;
        private final Boolean approved;
        private final String status;
        private final String providerCode;
    }
}