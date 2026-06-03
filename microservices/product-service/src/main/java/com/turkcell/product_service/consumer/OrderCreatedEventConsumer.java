package com.turkcell.product_service.consumer;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.turkcell.product_service.event.OrderCreatedEvent;
import com.turkcell.product_service.service.StockService;

@Configuration
public class OrderCreatedEventConsumer {

    private final StockService stockService;

    public OrderCreatedEventConsumer(StockService stockService) {
        this.stockService = stockService;
    }

    @Bean
    public Consumer<OrderCreatedEvent> consumeOrderCreatedEvent() {
        return event -> {
            System.out.println("\n🎯 OrderCreatedEvent Received!");
            stockService.processOrderItems(event);
        };
    }
}
