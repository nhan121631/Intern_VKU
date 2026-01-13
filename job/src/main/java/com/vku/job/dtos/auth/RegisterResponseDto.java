package com.vku.job.dtos.auth;

import lombok.Data;

@Data
public class RegisterResponseDto {
    private boolean success;
    private String message;
    private String email;

}
