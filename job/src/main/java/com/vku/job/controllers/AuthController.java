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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request) throws Exception {
        LoginResponseDto result = userService.login(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/google-login")
    public ResponseEntity<LoginResponseDto> googleLogin(@RequestBody @Valid GoogleLoginRequestDto request) {
        LoginResponseDto result = userService.googleLogin(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody @Valid RegisterRequestDto request)
            throws Exception {
        RegisterResponseDto result = userService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequestDto request) {
        userService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok().build();
    }

    // forgot password - send otp
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email) {
        userService.sendResetPasswordOtp(email);
        return ResponseEntity.ok().build();
    }

    // forgot password - check otp
    @PostMapping("/check-reset-password-otp")
    public ResponseEntity<Void> checkResetPasswordOtp(@RequestBody @Valid VerifyEmailRequestDto request) {
        userService.checkResetPasswordOtp(request);
        return ResponseEntity.ok().build();
    }

    // reset password
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

}