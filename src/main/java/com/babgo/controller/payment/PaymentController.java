package com.babgo.controller.payment;

import com.babgo.application.payment.PaymentFacade;
import com.babgo.application.payment.PaymentInfo;
import com.babgo.application.payment.PaymentWebhookHandler;
import com.babgo.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ocsp.Request;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;
    private final PaymentWebhookHandler paymentWebhookHandler;

    @PostMapping
    public ApiResponse<PaymentResponse.Ready> ready(
            @RequestBody PaymentRequest.Ready request
    ){
        //TODO 임시 값
        Long userid = 1L;
        PaymentInfo.Ready input = PaymentInfo.Ready.from(userid, request);
        PaymentInfo.ReadyResult output = paymentFacade.ready(input);
        PaymentResponse.Ready response =  PaymentResponse.Ready.from(output);
        return ApiResponse.success("결제 요청이 정상적으로 접수되었습니다.", response);
    }

    //세션 리다이렉트
    @GetMapping("/success")
    public ApiResponse<String> success(
            @RequestParam String paymentKey,
            @RequestParam UUID orderId,
            @RequestParam Long amount) {

        paymentFacade.pay(paymentKey ,orderId ,amount);

        log.info("결제를 처리 중입니다. 잠시 후 주문내역에서 상태를 확인하세요.");
        return ApiResponse.success("결제를 처리 중입니다. 잠시 후 주문내역에서 상태를 확인하세요.");
    }

    @GetMapping("/fail")
    public ApiResponse<String> fail(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message) {
        return ApiResponse.success("호출실패");
    }

    @PostMapping("/webhook")
    public ApiResponse<Void> webhook(
            @RequestBody PaymentRequest.WebhookPayload payload,
            @RequestHeader(value = "X-PG-Signature", required = false) String sig) {
        //TODO시그니처/필수필드 검증 (실패 시 빠르게 400/401)

        switch (payload.getEvent()) {
            case "PAYMENT_STATUS_CHANGED" -> paymentWebhookHandler.handleStatusChanged(payload);
            case "PAYMENT_CANCELLED"      -> paymentWebhookHandler.handleCancelled(payload);
            case "REFUND_COMPLETED"       -> paymentWebhookHandler.handleRefund(payload);
        }

        /**
         * pg 사에게 바로 리턴해주기
         */
        return ApiResponse.success("Webhook received successfully.");
    }


}
