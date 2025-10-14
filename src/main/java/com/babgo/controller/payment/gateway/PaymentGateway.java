package com.babgo.controller.payment.gateway;

import com.babgo.application.payment.PaymentInfo;
import com.babgo.repository.payment.client.ClientResponse;
import org.springframework.stereotype.Component;

@Component
public interface PaymentGateway {
    ClientResponse.create create(PaymentInfo.CreatePg input);
    void confirm(PaymentInfo.Confirm input);
}
