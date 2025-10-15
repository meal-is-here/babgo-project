package com.babgo.application.order;

import java.util.UUID;

public interface CancelWindow {
    void open(UUID orderId);
    boolean isOpen(UUID orderId);
    void close(UUID orderId);
}
