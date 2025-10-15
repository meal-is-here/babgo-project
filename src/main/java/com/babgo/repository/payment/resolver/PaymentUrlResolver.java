package com.babgo.repository.payment.resolver;

import com.babgo.application.payment.port.PaymentEndpointResolver;
import com.babgo.repository.payment.config.PaymentProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentUrlResolver implements PaymentEndpointResolver {

    private final PaymentProps props;

    public String approveUrl(UUID id){ return props.successBaseUrl() + "?orderId=" + id; }
    public String cancelUrl(UUID id){ return props.cancelUrl() +"/cancel/"+id; }
    public String refundUrl(UUID id){ return props.refundUrl()+"/refund/"+id; }
    public String failUrl(UUID id) {return props.failBaseUrl()+"?orderId="+id;}
    public String webhookUrl(){return props.webhookUrl();}
}