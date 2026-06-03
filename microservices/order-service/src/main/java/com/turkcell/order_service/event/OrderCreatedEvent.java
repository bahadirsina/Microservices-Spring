package com.turkcell.order_service.event;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String customerName,
    List<OrderItemEvent> items
) {}

record OrderItemEvent(
    UUID productId,
    int quantity,
    double price
) {}
