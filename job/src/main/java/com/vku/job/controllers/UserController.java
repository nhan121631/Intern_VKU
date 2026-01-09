package com.vku.job.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.User.FullNameUserResponse;
import com.vku.job.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/get-name")
    public ResponseEntity<List<FullNameUserResponse>> getUserName() {
        List<FullNameUserResponse> result = userService.getAllFullNameUsers();
        return ResponseEntity.ok(result);
    }
}
