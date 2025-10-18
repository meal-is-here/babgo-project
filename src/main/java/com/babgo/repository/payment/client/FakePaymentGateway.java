package com.babgo.repository.payment.client;

import com.babgo.application.payment.PaymentInfo;
import com.babgo.controller.payment.gateway.PaymentGateway;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FakePaymentGateway implements PaymentGateway {

    private final PgClient pgClient;

    @Override
    public ClientResponse.create create(PaymentInfo.CreatePg input) {
        log.info("[Payment Gateway] PG 세션 생성 요청 - orderId: {}", input.getOrderId());

        try {
            ClientRequest.create req = ClientRequest.create.from(input);
            ClientResponse.create res = pgClient.create(req);

            if (res.getCode() != 0) {
                throw new CustomException(ErrorCode.EXTERNAL, "PG 세션 생성 실패: " + res.getCode());
            }

            log.info("[Payment Gateway] PG 세션 생성 성공 - orderId: {}", input.getOrderId());
            return res;
        } catch (FeignException e) {
            log.error("[Payment Gateway] PG 세션 생성 실패 - orderId: {}, status: {}, message: {}",
                    input.getOrderId(), e.status(), e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL, "PG 세션 생성 실패: " + e.getMessage());
        }
    }

    @Override
    public void confirm(PaymentInfo.Confirm input) {
        log.info("[Payment Gateway] PG 결제 승인 요청 - paymentId: {}, idempotencyKey: {}",
                input.getPaymentId(), input.getIdempotencyKey());

        try {
            ClientRequest.confirm req = ClientRequest.confirm.from(input);
            pgClient.confirm(input.getIdempotencyKey(), req);
            log.info("[Payment Gateway] PG 결제 승인 요청 전송 완료 - paymentId: {}", input.getPaymentId());
        } catch (FeignException e) {
            log.error("[Payment Gateway] PG 결제 승인 요청 실패 - paymentId: {}, status: {}, message: {}",
                    input.getPaymentId(), e.status(), e.getMessage());
            throw new CustomException(ErrorCode.EXTERNAL, "결제 승인 요청 실패: " + e.getMessage());
        }
    }
}

