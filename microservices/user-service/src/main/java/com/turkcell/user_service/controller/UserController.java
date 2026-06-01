package com.turkcell.user_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/users")
@RestController
public class UserController {

    @GetMapping
    public String get() {
        System.out.println("UsersController çalıştı");
        return "UsersController";
    }
}