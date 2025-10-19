package com.babgo.controller.payment;

import com.babgo.application.payment.PaymentFacade;
import com.babgo.application.payment.PaymentInfo;
import com.babgo.application.payment.PaymentWebhookHandler;
import com.babgo.domain.user.User;
import com.babgo.global.api.ApiResponse;
import com.babgo.global.security.annotation.CurrentUser;
import com.babgo.global.security.annotation.RequireCustomer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;
    private final PaymentWebhookHandler paymentWebhookHandler;

    @RequireCustomer
    @PostMapping
    public ApiResponse<PaymentResponse.Ready> ready(
            @CurrentUser User user,
            @RequestBody PaymentRequest.Ready request
    ){
        PaymentInfo.Ready input = PaymentInfo.Ready.from(user.getUserId(), request);
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
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message) {

        log.info("[Payment Fail] 결제 실패 처리 - orderId: {}, code: {}, message: {}",
                orderId, code, message);

        if (orderId != null) {
            try {
                // 결제 실패 처리
                paymentWebhookHandler.handlePaymentFailure(orderId, code, message);

                log.info("[Payment Fail] 결제 실패 처리 완료 - orderId: {}", orderId);
                return ApiResponse.success("결제가 실패했습니다. 다시 시도해주세요.");

            } catch (Exception e) {
                log.error("[Payment Fail] 결제 실패 처리 중 오류 - orderId: {}, error: {}",
                        orderId, e.getMessage(), e);
                return ApiResponse.fail("결제 실패 처리 중 오류가 발생했습니다.", com.babgo.global.exception.ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        log.warn("[Payment Fail] orderId가 없음 - code: {}, message: {}", code, message);
        return ApiResponse.success("결제가 실패했습니다.");
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

        return ApiResponse.success("Webhook received successfully.");
    }


}
