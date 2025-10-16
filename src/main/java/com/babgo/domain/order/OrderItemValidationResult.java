package com.babgo.domain.order;

import com.babgo.application.order.OrderInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class OrderItemValidationResult {
    private final List<OrderItemSnapshot> validItems;
    private final List<OrderInfo.InvalidItem> invalidItems;
    public boolean hasInvalid() { return !invalidItems.isEmpty(); }
}