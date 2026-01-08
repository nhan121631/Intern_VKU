package com.vku.job.dtos.auth;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder({ "id", "username", "fullName", "isActive", "roles", "accessToken", "refreshToken" })
public class LoginResponseDto {
    private Long id;
    private String username;
    private String fullName;
    private int isActive;
    private List<String> roles;
    private String accessToken;
    private String refreshToken;
}
