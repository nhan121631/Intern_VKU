package com.vku.job.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.services.BrevoEmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "APIs for testing purposes")
public class TestController {

    @Autowired
    private BrevoEmailService brevoEmailService;

    @GetMapping("/hello")
    @Operation(summary = "Hello World", description = "A simple hello world endpoint for testing")
    public String hello() {
        return "Hello, World!";
    }

    @PostMapping("/test-email")
    @Operation(summary = "Test Email", description = "An endpoint to test email functionality")
    public String testEmail() {
        brevoEmailService.sendEmail("phamphunhanpham8@gmail.com", "Test Subject", "This is a test email.");
        return "Test email sent!";
    }
}