package com.babgo.application.order.event;

import java.util.UUID;

public record OrderExpiredEvent(UUID orderId) {}
