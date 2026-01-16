package com.vku.job.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.vku.job.dtos.user_profile.UpdateUserProfileRequestDto;
import com.vku.job.dtos.user_profile.UserProfileResponse;
import com.vku.job.entities.UserProfile;
import com.vku.job.repositories.UserProfileJpaRepository;

@Service
public class UserProfileService {
    @Autowired
    private UserProfileJpaRepository userProfileJpaRepository;

    // convert entity to dto
    private UserProfileResponse convertToDto(UserProfile userProfile) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(userProfile.getId());
        response.setFullName(userProfile.getFullName());
        response.setAvatarUrl(userProfile.getAvatar());
        response.setPhoneNumber(userProfile.getPhoneNumber());
        response.setAddress(userProfile.getAddress());
        return response;
    }

    // fetch user profile by user id
    public UserProfileResponse getUserProfileByUserId(Long userId) {

        UserProfile userProfile = userProfileJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return convertToDto(userProfile);
    }

    // update user profile
    public UserProfileResponse updateUserProfile(Long userId, MultipartFile avatar,
            UpdateUserProfileRequestDto userProfileRequest) {
        UserProfile userProfile = userProfileJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.getSize() > 2 * 1024 * 1024) {
                throw new RuntimeException("File size exceeds the maximum limit of 2MB");
            }
            if (!avatar.getContentType().startsWith("image/")) {
                throw new RuntimeException("Invalid file type. Only image files are allowed");
            }
            try {
                String fileName = System.currentTimeMillis() + "_" + avatar.getOriginalFilename();
                Path filePath = Paths.get("public/uploads/" + fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, avatar.getBytes());

                String avatarUrl = "/uploads/" + fileName;
                userProfile.setAvatar(avatarUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store avatar file", e);
            }
        }
        // if (avatar == null) {
        // userProfile.setAvatar(null);
        // }
        if (userProfileJpaRepository.existsByPhoneNumberAndUserIdNot(userProfileRequest.getPhoneNumber(), userId)) {
            throw new RuntimeException("Phone number already in use by another user");
        }

        userProfile.setFullName(userProfileRequest.getFullName());
        userProfile.setPhoneNumber(userProfileRequest.getPhoneNumber());
        userProfile.setAddress(userProfileRequest.getAddress());

        UserProfile updatedProfile = userProfileJpaRepository.save(userProfile);
        return convertToDto(updatedProfile);
    }

}