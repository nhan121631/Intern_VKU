package com.vku.job.dtos.user;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private LocalDateTime createdAt;
    private int isActive;
}
