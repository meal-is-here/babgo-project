package com.babgo.controller.payment;

import com.babgo.domain.payment.CardBrand;
import com.babgo.domain.payment.CardType;
import com.babgo.domain.payment.PaymentMethod;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentRequest {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Ready {
        private final UUID orderId;
        private final Long amount;
        private final PaymentMethod paymentType;
        private final CardBrand cardBrand;
        private final CardType cardType;
    }
}