package com.vku.job.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.auth.ChangePassRequestDto;
import com.vku.job.dtos.user.FullNameUserResponse;
import com.vku.job.dtos.user.NameUserResponse;
import com.vku.job.dtos.user.UserResponse;
import com.vku.job.dtos.user_profile.UpdateUserProfileRequestDto;
import com.vku.job.dtos.user_profile.UserProfileResponse;
import com.vku.job.services.UserProfileService;
import com.vku.job.services.UserService;
import com.vku.job.services.auth.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "APIs for user management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/get-name")
    @Operation(summary = "Get All User Names", description = "Retrieve a list of all users' full names")
    public ResponseEntity<List<FullNameUserResponse>> getUserName() {
        List<FullNameUserResponse> result = userService.getAllFullNameUsers();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-all")
    @Operation(summary = "Get All Users", description = "Retrieve a paginated list of all users")
    public ResponseEntity<PaginatedResponseDto<UserResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsersPaginated(page, size));
    }

    // change user status
    @PatchMapping("/change-status")
    @Operation(summary = "Change User Status", description = "Activate or deactivate a user account")
    public ResponseEntity<Void> changeUserStatus(@RequestParam("userId") Long userId,
            @RequestParam("isActive") int isActive) {
        userService.changeUserStatus(userId, isActive);
        return ResponseEntity.ok().build();
    }

    // get name user by id from token
    @GetMapping("/get-name-by-id")
    @Operation(summary = "Get My Name", description = "Retrieve the name of the authenticated user")
    public ResponseEntity<NameUserResponse> getNameUserById(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        NameUserResponse response = userService.getNameUserById(userId);
        return ResponseEntity.ok(response);
    }

    // get profile user by id from token
    @GetMapping("/get-profile")
    @Operation(summary = "Get My Profile", description = "Retrieve the profile of the authenticated user")
    public ResponseEntity<UserProfileResponse> getProfileUserById(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        UserProfileResponse response = userProfileService.getUserProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }

    // update profile user by id from token
    @PatchMapping(value = "/update-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update My Profile", description = "Update the profile of the authenticated user, including avatar upload")
    public ResponseEntity<UserProfileResponse> updateProfileUserById(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            @Valid @RequestPart("userProfileRequest") UpdateUserProfileRequestDto userProfileRequest) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);

        UserProfileResponse response = userProfileService.updateUserProfile(userId,
                avatar, userProfileRequest);

        return ResponseEntity.ok(response);
    }

    // change password
    @PatchMapping("/change-password")
    @Operation(summary = "Change My Password", description = "Change the password of the authenticated user")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid ChangePassRequestDto changePassRequest) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractUserIdFromToken(token);
        userService.changePassword(userId, changePassRequest);
        return ResponseEntity.ok().build();
    }

}
