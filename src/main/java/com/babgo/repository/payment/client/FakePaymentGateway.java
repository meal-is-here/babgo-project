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
        log.info("세션 요청실행");
        ClientRequest.create req = ClientRequest.create.from(input);
        ClientResponse.create res = pgClient.create(req);

        if (res.getCode() != 0) {
            throw new CustomException(ErrorCode.EXTERNAL, "PG 세션 생성 실패");
        }

        return res;
    }

    @Override
    public void confirm(PaymentInfo.Confirm input) {
        ClientRequest.confirm req = ClientRequest.confirm.from(input);
        try {
            pgClient.confirm(input.getIdempotencyKey(), req);
        }catch (FeignException e){
            //TODO 재시도 로직 넣기
            throw new CustomException(ErrorCode.EXTERNAL, "결제 승인 요청 실패");
        }

    }
}
