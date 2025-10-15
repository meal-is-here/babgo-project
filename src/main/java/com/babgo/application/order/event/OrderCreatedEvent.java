package com.babgo.application.order.event;

import java.util.UUID;

public record OrderCreatedEvent(UUID orderId) {
}
