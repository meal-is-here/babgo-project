package com.babgo.repository.payment.client;


import com.babgo.application.payment.PaymentInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

public class ClientRequest {

    @Getter
    @RequiredArgsConstructor
    public static class create {

        private final UUID orderId;
        private final Long amount;
        private final String desc;
        private final String successUrl;
        private final String failUrl;
        private final String resultCallback;
        public static create from(PaymentInfo.CreatePg input){
            return new create(
                    input.getOrderId(),
                    input.getAmount(),
                    input.getDesc(),
                    input.getSuccessUrl(),
                    input.getFailUrl(),
                    input.getResultCallback()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class confirm {
        private final String paymentKey;
        private final UUID paymentId;
        private final UUID orderId;
        private final Long amount;

        public static confirm from(PaymentInfo.Confirm input) {
        return new confirm(
                input.getPaymentKey(),
                input.getPaymentId(),
                input.getOrderId(),
                input.getAmount()
        );
        }
    }
}
