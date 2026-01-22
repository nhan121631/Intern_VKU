package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.auth.ChangePassRequestDto;
import com.vku.job.dtos.auth.ResetPasswordRequestDto;
import com.vku.job.dtos.user.FullNameUserResponse;
import com.vku.job.dtos.user.NameUserResponse;
import com.vku.job.dtos.user.UserResponse;
import com.vku.job.entities.User;
import com.vku.job.entities.UserProfile;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.repositories.projection.FullNameUserProjection;
import com.vku.job.services.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    // ======== GET ALL USER FULLNAME ========

    // get all user fullname - success case
    @Test
    void getAllFullNameUsers_success() {
        FullNameUserProjection p1 = new FullNameUserProjection() {
            public Long getId() {
                return 1L;
            }

            public String getFullName() {
                return "Nhan Pham";
            }
        };

        FullNameUserProjection p2 = new FullNameUserProjection() {
            public Long getId() {
                return 2L;
            }

            public String getFullName() {
                return "Van Teo";
            }
        };
        // given
        Mockito.when(userJpaRepository.getAllFullNameUser())
                .thenReturn(List.of(p1, p2));

        // when
        List<FullNameUserResponse> result = userService.getAllFullNameUsers();

        // then
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("Nhan Pham", result.get(0).getFullName());

        assertEquals(2L, result.get(1).getId());
        assertEquals("Van Teo", result.get(1).getFullName());

        verify(userJpaRepository).getAllFullNameUser();
    }

    // end get all user fullname - list is empty
    @Test
    void getAllFullNameUsers_emptyList() {
        // given
        Mockito.when(userJpaRepository.getAllFullNameUser())
                .thenReturn(List.of());

        // when
        List<FullNameUserResponse> result = userService.getAllFullNameUsers();

        // then
        assertEquals(0, result.size());

        verify(userJpaRepository).getAllFullNameUser();
    }

    // ======== END GET ALL USER FULLNAME ========

    // ======== GET ALL USERS PAGINATED ========

    private User createUser(Long id, String username, String fullName) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setIsActive(0);
        user.setCreatedAt(LocalDateTime.now());

        UserProfile profile = new UserProfile();
        profile.setFullName(fullName);
        profile.setUser(user);
        user.setProfile(profile);

        return user;
    }

    // get all users paginated - success case
    @Test
    void getAllUsersPaginated_success() {
        // given
        User u1 = createUser(1L, "nhan", "Nhan Pham");
        User u2 = createUser(2L, "teo", "Van Teo");

        Pageable pageable = PageRequest.of(0, 2);
        Page<User> page = new PageImpl<>(
                List.of(u1, u2),
                pageable,
                5 // total elements
        );

        Mockito.when(userJpaRepository.findAll(pageable))
                .thenReturn(page);

        // when
        PaginatedResponseDto<UserResponse> response = userService.getAllUsersPaginated(0, 2);

        // then
        assertEquals(2, response.getData().size());

        assertEquals(0, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());

        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());

        assertEquals("Nhan Pham", response.getData().get(0).getFullName());
        assertEquals("Van Teo", response.getData().get(1).getFullName());

        verify(userJpaRepository).findAll(pageable);
    }

    // end get all users paginated - empty page
    @Test
    void getAllUsersPaginated_emptyPage() {
        // given
        Pageable pageable = PageRequest.of(1, 2);
        Page<User> page = new PageImpl<>(
                List.of(),
                pageable,
                0 // total elements
        );

        Mockito.when(userJpaRepository.findAll(pageable))
                .thenReturn(page);

        // when
        PaginatedResponseDto<UserResponse> response = userService.getAllUsersPaginated(1, 2);

        // then
        assertEquals(0, response.getData().size());

        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(0, response.getTotalPages());

        assertFalse(response.isHasNext());
        assertTrue(response.isHasPrevious());

        verify(userJpaRepository).findAll(pageable);
    }
    // ======== END GET ALL USERS PAGINATED ========

    // ======== CHANGE USER ACTIVE STATUS ========

    // change user active status - success case active = 1
    @Test
    void changeUserStatus_success_active0() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(1);

        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.changeUserStatus(1L, 0);

        assertEquals(0, user.getIsActive());
        verify(userJpaRepository).save(user);
    }

    // change user active status - success case active = 0
    @Test
    void changeUserStatus_success_active1() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(0);

        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.changeUserStatus(1L, 1);

        assertEquals(1, user.getIsActive());
        verify(userJpaRepository).save(user);
    }

    // change user active status - user not found
    @Test
    void changeUserStatus_userNotFound() {
        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.changeUserStatus(1L, 1);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // change user active status - invalid status
    @Test
    void changeUserStatus_invalidStatus() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(1);

        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.changeUserStatus(1L, 2);
        });

        assertEquals("Invalid status value", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }
    // ======== END CHANGE USER ACTIVE STATUS ========

    // ======== FETCH NAME USER BY ID ========
    private User createUser(Long id, String fullName) {
        User user = new User();
        user.setId(id);

        UserProfile profile = new UserProfile();
        profile.setFullName(fullName);
        profile.setUser(user);

        user.setProfile(profile);
        return user;
    }

    // fetch name user by id - success case
    @Test
    void getNameUserById_success() {
        User user = createUser(1L, "Nhan Pham");

        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.of(user));

        NameUserResponse response = userService.getNameUserById(1L);

        assertNotNull(response);
        assertEquals("Nhan Pham", response.getFullName());

        verify(userJpaRepository).findById(1L);
    }

    // fetch name user by id - user not found
    @Test
    void getNameUserById_userNotFound() {
        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getNameUserById(1L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userJpaRepository).findById(1L);
    }

    // ======== END FETCH NAME USER BY ID ========

    // ======== CHANGE PASSWORD USER BY ID ========

    // change password - success case
    @Test
    void changePassword_success() {
        User user = createUser(1L, "Nhan Pham");
        user.setPassword("encoded-old");

        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.of(user));

        Mockito.when(passwordEncoder.matches("old123", "encoded-old"))
                .thenReturn(true);

        Mockito.when(passwordEncoder.encode("new123"))
                .thenReturn("encoded-new");

        userService.changePassword(
                1L,
                new ChangePassRequestDto("old123", "new123"));

        assertEquals("encoded-new", user.getPassword());

        verify(userJpaRepository).save(user);
    }

    // change password - user not found
    @Test
    void changePassword_userNotFound() {
        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(
                    1L,
                    new ChangePassRequestDto("old123", "new123"));
        });

        assertEquals("User not found", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // change password - old password incorrect
    @Test
    void changePassword_oldPasswordIncorrect() {
        User user = createUser(1L, "Nhan Pham");
        user.setPassword("encoded-old");
        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrong-old", "encoded-old"))
                .thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(
                    1L,
                    new ChangePassRequestDto("wrong-old", "new123"));
        });
        assertEquals("Old password is incorrect", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // change password - new password same as old password
    @Test
    void changePassword_newPasswordSameAsOld() {
        User user = createUser(1L, "Nhan Pham");
        user.setPassword("encoded-old");
        Mockito.when(userJpaRepository.findById(1L))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("old123", "encoded-old"))
                .thenReturn(true);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changePassword(
                    1L,
                    new ChangePassRequestDto("old123", "old123"));
        });
        assertEquals("New password must be different from old password", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // ======== END CHANGE PASSWORD USER BY ID ========

    // ======== RESET PASSWORD USER BY EMAIL ========
    private User createValidUser() {
        User user = new User();
        user.setEmail("a@gmail.com");
        user.setEmailVerified(true);
        user.setIsActive(0);
        user.setPassword("encoded-old");
        user.setEmailOtpHash("hashed-otp");
        user.setEmailOtpExpiry(System.currentTimeMillis() + 60_000);
        return user;
    }

    // reset password - success case
    @Test
    void resetPassword_success() {
        User user = createValidUser();

        Mockito.when(userJpaRepository.findByEmail("a@gmail.com"))
                .thenReturn(Optional.of(user));

        Mockito.when(passwordEncoder.matches("123456", "hashed-otp"))
                .thenReturn(true);

        Mockito.when(passwordEncoder.encode("new123"))
                .thenReturn("encoded-new");

        userService.resetPassword(
                new ResetPasswordRequestDto("a@gmail.com", "123456", "new123"));

        assertEquals("encoded-new", user.getPassword());
        assertNull(user.getEmailOtpHash());
        assertNull(user.getEmailOtpExpiry());

        verify(userJpaRepository).save(user);
    }

    // reset password - user not found
    @Test
    void resetPassword_userNotFound() {
        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.resetPassword(
                    new ResetPasswordRequestDto("nhan@gmail.com", "123456", "new123"));
        });
        assertEquals("User not found", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // reset password - email not verified
    @Test
    void resetPassword_emailNotVerified() {
        User user = new User();
        user.setEmail("nhan@gmail.com");
        user.setEmailVerified(false);
        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.resetPassword(
                    new ResetPasswordRequestDto("nhan@gmail.com", "123456", "new123"));
        });
        assertEquals("Email is not verified", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // reset password - account is deactivated
    @Test
    void resetPassword_accountDeactivated() {
        User user = new User();
        user.setEmail("nhan@gmail.com");
        user.setEmailVerified(true);
        user.setIsActive(1);
        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.resetPassword(
                    new ResetPasswordRequestDto("nhan@gmail.com", "123456", "new123"));
        });
        assertEquals("Your account is not active. Please contact support.", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // reset password - gg login
    @Test
    void resetPassword_ggLogin() {
        User user = new User();
        user.setEmail("nhan@gmail.com");
        user.setPassword(null);
        ;
        user.setEmailVerified(true);
        user.setIsActive(0);
        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.resetPassword(
                    new ResetPasswordRequestDto("nhan@gmail.com", "123456", "new123"));
        });
        assertEquals("Cannot reset password for Google login users", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // reset password - invalid OTP
    @Test
    void resetPassword_invalidOtp() {
        User user = createValidUser();

        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrong-otp", "hashed-otp"))
                .thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.resetPassword(
                    new ResetPasswordRequestDto("nhan@gmail.com", "wrong-otp", "new123"));
        });
        assertEquals("Invalid OTP", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }

    // reset password - expired OTP
    @Test
    void resetPassword_expiredOtp() {
        User user = createValidUser();
        user.setEmailOtpExpiry(System.currentTimeMillis() - 1); // expired

        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.resetPassword(
                    new ResetPasswordRequestDto("nhan@gmail.com", "123456", "new123"));
        });
        assertEquals("OTP has expired", exception.getMessage());
        verify(userJpaRepository, never()).save(any(User.class));
    }
}