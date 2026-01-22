package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.vku.job.dtos.user_profile.UpdateUserProfileRequestDto;
import com.vku.job.dtos.user_profile.UserProfileResponse;
import com.vku.job.entities.UserProfile;
import com.vku.job.repositories.UserProfileJpaRepository;
import com.vku.job.services.UserProfileService;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {
    @InjectMocks
    private UserProfileService userProfileService;
    @Mock
    private UserProfileJpaRepository userProfileJpaRepository;

    @Test
    void convertUserProfileToDto_success() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setFullName("Nhan Pham");
        profile.setAvatar("avatar.png");
        profile.setPhoneNumber("0123456789");
        profile.setAddress("Da Nang");

        UserProfileResponse response = userProfileService.convertToDto(profile);

        assertEquals(1L, response.getId());
        assertEquals("Nhan Pham", response.getFullName());
        assertEquals("avatar.png", response.getAvatarUrl());
        assertEquals("0123456789", response.getPhoneNumber());
        assertEquals("Da Nang", response.getAddress());
    }

    // ======= FETCH USER PROFILE BY USER ID =======
    private UserProfile createProfile() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setFullName("Nhan Pham");
        profile.setAvatar("avatar.png");
        profile.setPhoneNumber("0123456789");
        profile.setAddress("Da Nang");
        return profile;
    }

    // fetch user profile by user id - success
    @Test
    void getUserProfileByUserId_success() {
        UserProfile profile = createProfile();

        Mockito.when(userProfileJpaRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        UserProfileResponse response = userProfileService.getUserProfileByUserId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Nhan Pham", response.getFullName());
        assertEquals("avatar.png", response.getAvatarUrl());
        assertEquals("0123456789", response.getPhoneNumber());
        assertEquals("Da Nang", response.getAddress());

        verify(userProfileJpaRepository).findByUserId(1L);
    }

    // fetch user profile by user id - failure: user profile not found
    @Test
    void getUserProfileByUserId_notFound() {
        Mockito.when(userProfileJpaRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userProfileService.getUserProfileByUserId(1L));

        assertEquals("User profile not found", ex.getMessage());
    }

    // ======= END FETCH USER PROFILE BY USER ID =======

    // ======= UPDATE USER PROFILE =======

    private UpdateUserProfileRequestDto createRequest() {
        UpdateUserProfileRequestDto request = new UpdateUserProfileRequestDto();
        request.setFullName("New Name");
        request.setPhoneNumber("094044332");
        request.setAddress("New Address");
        return request;
    }

    // update user profile - failure: user profile not found
    @Test
    void updateUserProfile_profileNotFound() {
        Mockito.when(userProfileJpaRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userProfileService.updateUserProfile(
                        1L, null, createRequest()));

        assertEquals("User profile not found", ex.getMessage());
    }

    // update user profile - failure: image file size exceeds limit
    // (Skipping file upload tests for brevity)
    @Test
    void updateUserProfile_avatarTooLarge() {
        UserProfile profile = createProfile();

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "a.png",
                "image/png",
                new byte[2 * 1024 * 1024 + 1]);

        Mockito.when(userProfileJpaRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userProfileService.updateUserProfile(
                        1L, avatar, createRequest()));

        assertEquals(
                "File size exceeds the maximum limit of 2MB",
                ex.getMessage());
    }

    // update user profile - failure: avata file type invalid
    @Test
    void updateUserProfile_invalidAvatarType() {
        UserProfile profile = createProfile();

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "a.txt",
                "text/plain",
                "abc".getBytes());

        Mockito.when(userProfileJpaRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userProfileService.updateUserProfile(
                        1L, avatar, createRequest()));

        assertEquals(
                "Invalid file type. Only image files are allowed",
                ex.getMessage());
    }

    // update user profile - failure: phone number already in use
    @Test
    void updateUserProfile_phoneAlreadyExists() {
        UserProfile profile = createProfile();

        Mockito.when(userProfileJpaRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        Mockito.when(
                userProfileJpaRepository.existsByPhoneNumberAndUserIdNot(
                        "094044332", 1L))
                .thenReturn(true);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userProfileService.updateUserProfile(
                        1L, null, createRequest()));

        assertEquals(
                "Phone number already in use by another user",
                ex.getMessage());
    }

    // update user profile - success
    @Test
    void updateUserProfile_success_withoutAvatar() {
        UserProfile profile = createProfile();

        Mockito.when(userProfileJpaRepository.findByUserId(1L))
                .thenReturn(Optional.of(profile));

        Mockito.when(
                userProfileJpaRepository.existsByPhoneNumberAndUserIdNot(
                        "094044332", 1L))
                .thenReturn(false);

        Mockito.when(userProfileJpaRepository.save(profile))
                .thenReturn(profile);

        UserProfileResponse response = userProfileService.updateUserProfile(
                1L, null, createRequest());

        assertEquals("New Name", response.getFullName());
        assertEquals("094044332", response.getPhoneNumber());
        assertEquals("New Address", response.getAddress());

        verify(userProfileJpaRepository).save(profile);
    }

    // ======= END UPDATE USER PROFILE =======

}
