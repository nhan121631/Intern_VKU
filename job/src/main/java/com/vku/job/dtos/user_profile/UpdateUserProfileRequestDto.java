package com.vku.job.dtos.user_profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequestDto {

    @NotNull(message = "Id is required")
    private Long id;
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    private String fullName;
    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;
    private String address;

}
