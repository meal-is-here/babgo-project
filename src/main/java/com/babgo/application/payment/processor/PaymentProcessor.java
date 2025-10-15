package com.babgo.application.payment.processor;

import com.babgo.domain.payment.PaymentMethod;

public interface PaymentProcessor {
    /**
     * 선택 기준
     */
    boolean supports(PaymentMethod method);

    /**
     * 수단별 절차
     */
    void process(PaymentProcessContext ctx);
}
