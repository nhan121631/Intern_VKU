package com.vku.job.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.auth.GoogleLoginRequestDto;
import com.vku.job.dtos.auth.LoginRequestDto;
import com.vku.job.dtos.auth.LoginResponseDto;
import com.vku.job.dtos.auth.RegisterRequestDto;
import com.vku.job.dtos.auth.RegisterResponseDto;
import com.vku.job.dtos.auth.ResetPasswordRequestDto;
import com.vku.job.dtos.auth.VerifyEmailRequestDto;
import com.vku.job.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request) throws Exception {
        LoginResponseDto result = userService.login(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/google-login")
    @Operation(summary = "Google Login", description = "Authenticate user via Google and return JWT token")
    public ResponseEntity<LoginResponseDto> googleLogin(@RequestBody @Valid GoogleLoginRequestDto request) {
        LoginResponseDto result = userService.googleLogin(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Register a new user and send verification email")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid RegisterRequestDto request)
            throws Exception {
        RegisterResponseDto result = userService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify Email", description = "Verify user's email using OTP")
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequestDto request) {
        userService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok().build();
    }

    // forgot password - send otp
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password", description = "Send OTP to user's email for password reset")
    public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email) {
        userService.sendResetPasswordOtp(email);
        return ResponseEntity.ok().build();
    }

    // forgot password - check otp
    @PostMapping("/check-reset-password-otp")
    @Operation(summary = "Check Reset Password OTP", description = "Check the OTP sent for password reset")
    public ResponseEntity<Void> checkResetPasswordOtp(@RequestBody @Valid VerifyEmailRequestDto request) {
        userService.checkResetPasswordOtp(request);
        return ResponseEntity.ok().build();
    }

    // reset password
    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Reset user's password using OTP")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

}