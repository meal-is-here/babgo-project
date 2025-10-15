package com.babgo.application.payment;

import com.babgo.application.payment.processor.PaymentProcessContext;
import com.babgo.controller.payment.PaymentRequest;
import com.babgo.domain.payment.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentInfo {

    /**
     * Input
     */
    @Getter
    @RequiredArgsConstructor
    public static class Ready {
        private final Long userId;
        private final UUID orderId;
        private final Long amount;
        private final PaymentMethod paymentType;
        private final CardBrand cardBrand;
        private final CardType cardType;
        public static Ready from(
                Long userId,
                PaymentRequest.Ready request
        ){
            return new Ready(
                    userId,
                    request.getOrderId(),
                    request.getAmount(),
                    request.getPaymentType(),
                    request.getCardBrand(),
                    request.getCardType()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CreatePg {

        private final UUID orderId;
        private final Long amount;
        private final String desc;
        private final String successUrl;
        private final String failUrl;
        private final String resultCallback;

        public static CreatePg from(Payment readPayment,String successUrl, String failUrl, String webhookUrl){
            return new CreatePg(
                readPayment.getOrderId(),
                readPayment.getAmount(),
                "메뉴 설명",
                    successUrl,
                    failUrl,
                    webhookUrl
            );
        }

    }

    @Getter
    @RequiredArgsConstructor
    public static class Confirm {
        private final String idempotencyKey;
        private final String paymentKey;
        private final UUID paymentId;
        private final UUID orderId;
        private final Long amount;

        public static Confirm from(String idempotencyKey, PaymentProcessContext ctx){
            return new Confirm(
                    idempotencyKey,
                    ctx.paymentKey(),
                    ctx.paymentId(),
                    ctx.orderId(),
                    ctx.amount()
            );
        }
    }
    /**
     * Output
     */
    @Getter
    @RequiredArgsConstructor
    public static class ReadyResult {
        private final UUID paymentId;
        private final PaymentStatus paymentStatus;
        private final String checkoutPage;

        public static ReadyResult from(Payment payment, String checkoutPage){
            return new ReadyResult(
              payment.getPaymentId(),
              payment.getPaymentStatus(),
              checkoutPage
            );
        }
    }

}
