package com.turkcell.order_service.model;

import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private String customerName;
    private List<OrderItem> items;
    private OrderStatus status;

    public Order() {}

    public Order(String customerName, List<OrderItem> items) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.items = items;
        this.status = OrderStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, FAILED, COMPLETED
    }
}
