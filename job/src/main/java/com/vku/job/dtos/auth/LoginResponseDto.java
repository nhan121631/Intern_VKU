package com.vku.job.dtos.auth;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDto {
    private UserInfo loggedInUser;
    private String accessToken;
    private String refreshToken;

    @Getter
    @Setter
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private int isActive;
        private List<String> roles;
    }
}
