package com.turkcell.product_service.consumer;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.turkcell.product_service.event.OrderCreatedEvent;

@Configuration
public class OrderCreatedEventConsumer {

    @Bean
    public Consumer<OrderCreatedEvent> consumeOrderCreatedEvent() {
        return event -> {
            System.out.println("\n🎯 OrderCreatedEvent Received!");
            System.out.println("Order ID: " + event.orderId());
        };
    }
}
