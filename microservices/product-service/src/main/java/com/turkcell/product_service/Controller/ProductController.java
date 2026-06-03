package com.turkcell.product_service.Controller;

import java.util.UUID;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkcell.product_service.event.TestEvent;

@RequestMapping("/api/products")
@RestController
public class ProductController {

    private final StreamBridge streamBridge;

    public ProductController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @GetMapping
    public String test(@PathVariable String message) {
        var event = new TestEvent(message, UUID.randomUUID());
        streamBridge.send("testEvent-out-0", event);
        return "Başarılı";
    }

}