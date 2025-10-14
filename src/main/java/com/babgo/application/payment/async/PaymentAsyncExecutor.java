package com.babgo.application.payment.async;
import com.babgo.application.payment.processor.PaymentProcessContext;
import com.babgo.application.payment.processor.PaymentProcessor;
import com.babgo.domain.order.Order;
import com.babgo.domain.order.OrderService;
import com.babgo.domain.payment.Payment;
import com.babgo.domain.payment.PaymentService;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAsyncExecutor {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final List<PaymentProcessor> processors;

    //비동기로 외부 pg를 호출만 함
    @Async("paymentExecutor")
    public void requestPgAsync(UUID paymentId, String paymentKey, Long amount) {
        // 비동기 스레드에서 다시 로드(영속성/트랜잭션 경계 안전)
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
        } catch (Exception e) {
            log.error("[Async Payment Error] PG 호출 중 오류 발생 - paymentId: {}", paymentId, e);
            // TODO: 재시도 큐 등록 or PaymentScheduler로 실패 감시
        }
    }

}
