package com.turkcell.order_service.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/orders")
@RestController
public class OrderController {

    @PostMapping
    public String createOrder() {
        return "Order service disabled";
    }
}
