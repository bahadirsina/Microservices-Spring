package com.turkcell.order_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.turkcell.order_service.event.OrderCreatedEvent;
import com.turkcell.order_service.model.Order;
import com.turkcell.order_service.model.OrderItem;

@Service
public class OrderService {
    
    private final StreamBridge streamBridge;

    public OrderService(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public Order createOrder(String customerName, List<OrderItem> items) {
        Order order = new Order(customerName, items);
        
        // OrderCreatedEvent oluştur ve publish et
        var orderCreatedEvent = new OrderCreatedEvent(
            order.getId(),
            order.getCustomerName(),
            items.stream()
                .map(item -> new com.turkcell.order_service.event.OrderItemEvent(
                    item.getProductId(),
                    item.getQuantity(),
                    item.getPrice()
                ))
                .collect(Collectors.toList())
        );

        // Kafka topic'ine gönder
        streamBridge.send("orderCreatedEvent-out-0", orderCreatedEvent);
        
        System.out.println("Sipariş oluşturuldu: " + order.getId() + " - Event publish edildi");
        
        return order;
    }
}
