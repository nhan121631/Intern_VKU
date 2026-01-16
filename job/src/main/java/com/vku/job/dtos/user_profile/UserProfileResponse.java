package com.vku.job.dtos.user_profile;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String avatarUrl;
    private String phoneNumber;
    private String address;

}
