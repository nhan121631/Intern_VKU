package com.vku.job.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "APIs for testing purposes")
public class TestController {
    
    @GetMapping("/hello")
    @Operation(summary = "Hello World", description = "A simple hello world endpoint for testing")
    public String hello() {
        return "Hello, World!";
    }
}
