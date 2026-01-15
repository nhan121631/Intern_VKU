package com.vku.job.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.user.FullNameUserResponse;
import com.vku.job.dtos.user.NameUserResponse;
import com.vku.job.dtos.user.UserResponse;
import com.vku.job.services.UserService;
import com.vku.job.services.auth.JwtService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/get-name")
    public ResponseEntity<List<FullNameUserResponse>> getUserName() {
        List<FullNameUserResponse> result = userService.getAllFullNameUsers();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-all")
    public ResponseEntity<PaginatedResponseDto<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsersPaginated(page, size));
    }

    // change user status
    @PatchMapping("/change-status")
    public ResponseEntity<Void> changeUserStatus(@RequestParam("userId") Long userId,
            @RequestParam("isActive") int isActive) {
        userService.changeUserStatus(userId, isActive);
        return ResponseEntity.ok().build();
    }

    // get name user by id from token
    @GetMapping("/get-name-by-id")
    public ResponseEntity<NameUserResponse> getNameUserById(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        NameUserResponse response = userService.getNameUserById(userId);
        return ResponseEntity.ok(response);
    }
}
