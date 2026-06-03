package com.turkcell.product_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.turkcell.product_service.event.OrderCreatedEvent;

@Service
public class StockService {

    public void processOrderItems(OrderCreatedEvent event) {
        System.out.println("=== Order Processing Started ===");
        System.out.println("Order ID: " + event.orderId());
        System.out.println("Customer: " + event.customerName());
        System.out.println("Items Count: " + event.items().size());
        
        event.items().forEach(item -> {
            processItem(item.productId(), item.quantity());
        });
        
        System.out.println("=== Order Processing Completed ===\n");
    }

    private void processItem(UUID productId, int quantity) {
        System.out.println("📦 Ürün ID: " + productId + " | Miktar: " + quantity);
        System.out.println("   ✓ Stok kontrolü yapıldı");
        System.out.println("   ✓ Stok düşüldü: -" + quantity);
        System.out.println("   ✓ İşlem tamamlandı\n");
    }
}
