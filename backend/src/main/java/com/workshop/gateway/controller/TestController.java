package com.workshop.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/public/hello")
    public String publicEndpoint() {
        return "Hello! This endpoint is public.";
    }

    @GetMapping("/api/hello")
    public String protectedEndpoint() {
        return "Hello! You are authenticated.";
    }
}