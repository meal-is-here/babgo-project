package com.babgo.domain.payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findById(UUID paymentId);
}
