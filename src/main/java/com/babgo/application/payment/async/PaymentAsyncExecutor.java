package com.babgo.application.payment.async;
import com.babgo.application.payment.processor.PaymentProcessContext;
import com.babgo.application.payment.processor.PaymentProcessor;
import com.babgo.domain.payment.Payment;
import com.babgo.domain.payment.PaymentService;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAsyncExecutor {

    private final PaymentService paymentService;
    private final List<PaymentProcessor> processors;
    @Async("paymentExecutor")
    public void requestPgAsync(UUID paymentId, String paymentKey, Long amount) {
        log.info("[Payment Async] 결제 처리 시작 - paymentId: {}, paymentKey: {}, amount: {}",
                paymentId, paymentKey, amount);

        Payment payment = paymentService.getPayment(paymentId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.PAYMENT_NOT_FOUND,
                        "결제 건을 찾을 수 없습니다.")
                );

        PaymentProcessor processor = processors.stream()
                .filter(pro-> pro.supports(payment.getMethod()))
                .findFirst()
                .orElseThrow(
                        () -> new CustomException(
                                ErrorCode.BAD_REQUEST,
                                "지원하지 않는 결제 수단입니다: " + payment.getMethod()));

        try {
            processor.process(PaymentProcessContext.of(payment, paymentKey, amount));
            log.info("[Payment Async] PG 결제 승인 요청 전송 완료 - paymentId: {} (결과는 웹훅으로 수신 예정)", paymentId);
        } catch (Exception e) {
            log.error("[Payment Async] PG 결제 승인 요청 전송 실패 - paymentId: {}, error: {}",
                    paymentId, e.getMessage(), e);
            handlePaymentFailure(paymentId, e);
        }
    }


    /**
     * 결제 실패 시 처리
     */
    private void handlePaymentFailure(UUID paymentId, Exception e) {
        try {
            Payment payment = paymentService.getPayment(paymentId)
                    .orElse(null);

            if (payment != null) {
                payment.markFailed();
                log.error("[Payment Async] 결제 상태를 FAILED로 변경 - paymentId: {}", paymentId);
            }
        } catch (Exception ex) {
            log.error("[Payment Async] 결제 실패 상태 변경 중 오류 - paymentId: {}, error: {}",
                    paymentId, ex.getMessage(), ex);
        }
    }

}
