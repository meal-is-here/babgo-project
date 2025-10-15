package com.babgo.domain.payment;

import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public Payment create(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Optional<Payment> getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Transactional
    public boolean startPayment(UUID paymentId) {
        try {
            Payment payment = getPayment(paymentId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "결제 없음"));

            if (payment.getPaymentStatus() != PaymentStatus.READY)
                return false;

            payment.markProcessing();
            return true;

        } catch (OptimisticLockException e) {
            return false;
        }
    }

    public void markApproved(Payment payment, String transactionKey) {
        payment.markApproved(transactionKey);
    }
}
