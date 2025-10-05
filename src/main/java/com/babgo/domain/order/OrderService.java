package com.babgo.domain.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public UUID createOrderId() {
        return orderRepository.findNextOrderId();
    }

    public Order create(Order order) {
        return orderRepository.save(order);
    }

    /**
     *
     *  전제: items 는 이미 검증/재고확보 완료 (null 아님, 단가>=0, 수량>=1 보장).
     *  역할: 합계 계산 + long 오버플로 감지.
     */
    public Long calculateTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return 0L;

        long sum = 0L;
        for (OrderItem i : items) {
            long line = Math.multiplyExact(i.getPrice(), i.getQuantity());
            sum = Math.addExact(sum, line);
        }

        return sum;
    }

    //TODO 주문 목록 조회

    //TODO 주문 단건 조회
}
