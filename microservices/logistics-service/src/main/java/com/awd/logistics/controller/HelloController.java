package com.awd.logistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistics")
@Tag(name = "Logistics", description = "Health / greeting endpoint of the Logistics microservice")
public class HelloController {

    @GetMapping("/hi")
    @Operation(summary = "Say hi", description = "Checks that the Logistics microservice is up and returns a greeting.")
    @ApiResponse(responseCode = "200", description = "Greeting message")
    public String hi() {
        return "Hi, from Ines";
    }
}
