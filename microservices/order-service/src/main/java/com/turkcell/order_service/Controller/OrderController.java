package com.turkcell.order_service.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkcell.order_service.model.Order;
import com.turkcell.order_service.model.OrderItem;
import com.turkcell.order_service.service.OrderService;

@RequestMapping("/api/orders")
@RestController
public class OrderController {
    
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request.getCustomerName(), request.getItems());
    }

    public static class CreateOrderRequest {
        private String customerName;
        private List<OrderItem> items;

        public CreateOrderRequest() {}

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
    }
}
