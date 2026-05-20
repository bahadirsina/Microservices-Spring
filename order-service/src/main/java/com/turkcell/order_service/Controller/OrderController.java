package com.turkcell.order_service.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello customer-service";
    }
}
