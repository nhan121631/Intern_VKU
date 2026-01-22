package com.vku.job.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePassRequestDto {

    @NotBlank(message = "Old password must not be blank")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Old password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String oldPassword;

    @NotBlank(message = "New password must not be blank")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "New password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String newPassword;
}
