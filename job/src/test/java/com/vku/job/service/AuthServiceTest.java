package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.vku.job.dtos.auth.GoogleLoginRequestDto;
import com.vku.job.dtos.auth.LoginRequestDto;
import com.vku.job.dtos.auth.LoginResponseDto;
import com.vku.job.dtos.auth.RegisterRequestDto;
import com.vku.job.dtos.auth.RegisterResponseDto;
import com.vku.job.dtos.user.UserResponse;
import com.vku.job.entities.Role;
import com.vku.job.entities.User;
import com.vku.job.entities.UserProfile;
import com.vku.job.exceptions.HttpException;
import com.vku.job.repositories.RoleJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.services.MailService;
import com.vku.job.services.UserService;
import com.vku.job.services.auth.JwtService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RoleJpaRepository roleJpaRepository;

    @Mock
    private MailService emailService;

    @InjectMocks
    private UserService authService;

    // Test convertToDto method
    @Test
    void convertToDto_success() {
        // given
        UserProfile profile = new UserProfile();
        profile.setFullName("Nhan Pham");

        User user = new User();
        user.setId(1L);
        user.setUsername("nhan");
        user.setProfile(profile);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(0);

        // when
        UserResponse dto = authService.convertToDto(user);

        // then
        assertEquals(1L, dto.getId());
        assertEquals("nhan", dto.getUsername());
        assertEquals("Nhan Pham", dto.getFullName());
        assertEquals(0, dto.getIsActive());
        assertNotNull(dto.getCreatedAt());
    }

    // ========= LOGIN CREDENTIALS TESTS ==========
    // Login test success
    @Test
    void login_success() throws Exception {
        LoginRequestDto request = new LoginRequestDto("vanteo", "Nhan@123456");

        Role role = new Role();
        role.setName("Users");

        User user = new User();
        user.setId(1L);
        user.setUsername("vanteo");
        user.setEmail("phamphunhan624@gmail.com");
        user.setPassword("encoded-pass");
        user.setEmailVerified(true);
        user.setIsActive(0);
        user.setRoles(List.of(role));

        Mockito.when(userJpaRepository.findByUsername("vanteo"))
                .thenReturn(Optional.of(user));

        Mockito.when(passwordEncoder.matches("Nhan@123456", "encoded-pass"))
                .thenReturn(true);

        Mockito.when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        Mockito.when(jwtService.generateRefreshToken(user))
                .thenReturn("refresh-token");

        LoginResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        assertEquals(1L, response.getLoggedInUser().getId());
        assertEquals("vanteo", response.getLoggedInUser().getUsername());
        assertEquals("phamphunhan624@gmail.com", response.getLoggedInUser().getEmail());
        assertEquals(0, response.getLoggedInUser().getIsActive());
        assertEquals(List.of("Users"), response.getLoggedInUser().getRoles());
    }

    // login user is not found
    @Test
    void login_userNotFound() {
        LoginRequestDto request = new LoginRequestDto("unknown", "somepass");
        Mockito.when(userJpaRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());
        try {
            authService.login(request);
        } catch (Exception e) {
            assertEquals("Invalid username or password", e.getMessage());
        }
    }

    // password mismatch
    @Test
    void login_invalidPassword() {
        LoginRequestDto request = new LoginRequestDto("vanteo", "wrongpass");
        User user = new User();
        user.setUsername("vanteo");
        user.setPassword("encoded-pass");
        Mockito.when(userJpaRepository.findByUsername("vanteo"))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrongpass", "encoded-pass"))
                .thenReturn(false);
        try {
            authService.login(request);
        } catch (Exception e) {
            assertEquals("Invalid username or password", e.getMessage());
        }
    }

    // login with account not active
    @Test
    void login_accountNotActive() {
        LoginRequestDto request = new LoginRequestDto("vanteo", "Nhan@123456");
        User user = new User();
        user.setUsername("vanteo");
        user.setEmailVerified(true);
        user.setPassword("encoded-pass");
        user.setIsActive(1); // account not active
        Mockito.when(userJpaRepository.findByUsername("vanteo"))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("Nhan@123456", "encoded-pass"))
                .thenReturn(true);
        try {
            authService.login(request);
        } catch (Exception e) {
            assertEquals("Your account is not active. Please contact support.", e.getMessage());
        }
    }

    // email not verified
    @Test
    void login_emailNotVerified() {
        LoginRequestDto request = new LoginRequestDto("vanteo", "Nhan@123456");
        User user = new User();
        user.setUsername("vanteo");
        user.setEmailVerified(false);
        user.setPassword("encoded-pass");
        Mockito.when(userJpaRepository.findByUsername("vanteo"))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("Nhan@123456", "encoded-pass"))
                .thenReturn(true);
        try {
            authService.login(request);
        } catch (Exception e) {
            assertEquals("Email is not verified. Please verify your email before logging in.", e.getMessage());
        }
    }

    // login role not found
    @Test
    void login_roleNotFound() {
        LoginRequestDto request = new LoginRequestDto("vanteo", "Nhan@123456");
        User user = new User();
        user.setUsername("vanteo");
        user.setEmailVerified(true);
        user.setPassword("encoded-pass");
        user.setIsActive(0);
        user.setRoles(List.of()); // no roles
        Mockito.when(userJpaRepository.findByUsername("vanteo"))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("Nhan@123456", "encoded-pass"))
                .thenReturn(true);
        try {
            authService.login(request);
        } catch (Exception e) {
            assertEquals("User has no roles assigned", e.getMessage());
        }
    }
    // ========= END LOGIN CREDENTIALS TESTS ==========

    // ========= LOGIN GOOGLE TESTS ==========

    // Login with Google success
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_success() throws Exception {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("token");

        Map<String, Object> payload = Map.of(
                "email", "test@gmail.com",
                "name", "Test User",
                "iss", "https://accounts.google.com",
                "exp", System.currentTimeMillis() / 1000 + 1000);

        ResponseEntity<Map> response = new ResponseEntity<>(payload, HttpStatus.OK);

        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);

        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrueAndPasswordIsNotNull(Mockito.anyString()))
                .thenReturn(false);

        Mockito.when(userJpaRepository.findByUsername("test@gmail.com"))
                .thenReturn(Optional.empty());

        Role role = new Role();
        role.setName("Users");
        Mockito.when(roleJpaRepository.findByName("Users"))
                .thenReturn(Optional.of(role));

        Mockito.when(jwtService.generateAccessToken(Mockito.any()))
                .thenReturn("access");

        Mockito.when(jwtService.generateRefreshToken(Mockito.any()))
                .thenReturn("refresh");

        // when
        LoginResponseDto result = authService.googleLogin(dto);

        // then
        assertNotNull(result);
        assertEquals("access", result.getAccessToken());
        assertEquals("refresh", result.getRefreshToken());

        verify(userJpaRepository).save(Mockito.any(User.class));
    }

    // Login with Google - invalid token
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_invalidToken() {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("invalid-token");
        ResponseEntity<Map> response = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);

        Exception exception = assertThrows(HttpException.class, () -> {
            authService.googleLogin(dto);
        });

        assertEquals("Invalid Google token", exception.getMessage());
    }

    // Login with Google - token expired
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_tokenExpired() {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("expired-token");
        Map<String, Object> payload = Map.of(
                "email", "phamphunha@gmail.com",
                "name", "Test User",
                "iss", "https://accounts.google.com",
                "exp", System.currentTimeMillis() / 1000 - 1000); // expired
        ResponseEntity<Map> response = new ResponseEntity<>(payload, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);
        Exception exception = assertThrows(HttpException.class, () -> {
            authService.googleLogin(dto);
        });

        assertEquals("Google token has expired", exception.getMessage());
    }

    // Login with Google - invalid issuer
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_invalidIssuer() {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("token");
        Map<String, Object> payload = Map.of(
                "email", "phamnhan@gmail.com",
                "name", "Test User",
                "iss", "https://invalid-issuer.com",
                "exp", System.currentTimeMillis() / 1000 + 1000);
        ResponseEntity<Map> response = new ResponseEntity<>(payload, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);
        Exception exception = assertThrows(HttpException.class, () -> {
            authService.googleLogin(dto);
        });
        assertEquals("Invalid Google token issuer", exception.getMessage());
    }

    // Login with Google - email not found in token
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_emailNotFound() {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("token");
        Map<String, Object> payload = Map.of(
                "name", "Test User",
                "iss", "https://accounts.google.com",
                "exp", System.currentTimeMillis() / 1000 + 1000);
        ResponseEntity<Map> response = new ResponseEntity<>(payload, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);
        Exception exception = assertThrows(HttpException.class, () -> {
            authService.googleLogin(dto);
        });
        assertEquals("Email not found in token", exception.getMessage());
    }

    // Login with Google - account exists with password (not Google account)
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_accountExistsWithPassword() {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("token");
        Map<String, Object> payload = Map.of(
                "email", "phamphunhan@gmail.com",
                "name", "Test User",
                "iss", "https://accounts.google.com",
                "exp", System.currentTimeMillis() / 1000 + 1000);
        ResponseEntity<Map> response = new ResponseEntity<>(payload, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrueAndPasswordIsNotNull("phamphunhan@gmail.com"))
                .thenReturn(true);
        Exception exception = assertThrows(HttpException.class, () -> {
            authService.googleLogin(dto);
        });
        assertEquals("Email already registered. Please login with your credentials.",
                exception.getMessage());
    }

    // Login with Google - account not active
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_accountNotActive() {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("token");
        Map<String, Object> payload = Map.of(
                "email", "phamphunhan@gmail.com",
                "name", "Test User",
                "iss", "https://accounts.google.com",
                "exp", System.currentTimeMillis() / 1000 + 1000);
        ResponseEntity<Map> response = new ResponseEntity<>(payload, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrueAndPasswordIsNotNull("phamphunhan@gmail.com"))
                .thenReturn(false);
        User user = new User();
        user.setIsActive(1); // account not active
        Mockito.when(userJpaRepository.findByUsername("phamphunhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Exception exception = assertThrows(HttpException.class, () -> {
            authService.googleLogin(dto);
        });
        assertEquals("Your account is not active. Please contact support.", exception.getMessage());
    }

    // Login with Google - role "Users" not found
    @SuppressWarnings("rawtypes")
    @Test
    void loginWithGoogle_roleUsersNotFound() {
        GoogleLoginRequestDto dto = new GoogleLoginRequestDto("token");
        Map<String, Object> payload = Map.of(
                "email", "phamphunhan@gmail.com",
                "name", "Test User",
                "iss", "https://accounts.google.com",
                "exp", System.currentTimeMillis() / 1000 + 1000);
        ResponseEntity<Map> response = new ResponseEntity<>(payload, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(
                Mockito.anyString(), Mockito.eq(Map.class)))
                .thenReturn(response);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrueAndPasswordIsNotNull("phamphunhan@gmail.com"))
                .thenReturn(false);
        Mockito.when(userJpaRepository.findByUsername("phamphunhan@gmail.com"))
                .thenReturn(Optional.empty());
        Mockito.when(roleJpaRepository.findByName("Users"))
                .thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.googleLogin(dto);
        });
        assertEquals("Role USER not found", exception.getMessage());
    }
    // ========= END LOGIN GOOGLE TESTS ==========

    // ========= REGISTER CREDENTIALS TESTS ==========

    // Register user success
    @Test
    void register_newUser_success() throws Exception {
        // given
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("nhan");
        dto.setEmail("nhan@gmail.com");
        dto.setPassword("123456");
        dto.setFullName("Nhan Pham");

        Mockito.when(userJpaRepository.existsByUsernameAndEmailVerifiedTrue("nhan"))
                .thenReturn(false);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrue("nhan@gmail.com"))
                .thenReturn(false);

        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.empty());

        Mockito.when(passwordEncoder.encode(Mockito.anyString()))
                .thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        Role role = new Role();
        role.setName("Users");

        Mockito.when(roleJpaRepository.findByName("Users"))
                .thenReturn(Optional.of(role));

        // when
        RegisterResponseDto response = authService.register(dto);

        // then
        assertTrue(response.isSuccess());
        assertEquals("nhan@gmail.com", response.getEmail());
        assertEquals(
                "Registration successful. Please check your email for the verification code.",
                response.getMessage());

        verify(userJpaRepository).save(any(User.class));
        verify(emailService).sendVerificationCode(
                eq("nhan@gmail.com"),
                anyString());
    }

    // Register user - username already exists
    @Test
    void register_usernameExists() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("existingUser");
        dto.setEmail("phamnhan@gmail.com");
        dto.setPassword("SomePass@123");
        dto.setFullName("Pham Nhan");
        Mockito.when(userJpaRepository.existsByUsernameAndEmailVerifiedTrue("existingUser"))
                .thenReturn(true);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(dto);
        });
        assertEquals("Username already exists", exception.getMessage());
    }

    // Register user - email already exists
    @Test
    void register_emailExists() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("newuser");
        dto.setEmail("existingemail@gmail.com");
        dto.setPassword("SomePass@123");
        dto.setFullName("New User");
        Mockito.when(userJpaRepository.existsByUsernameAndEmailVerifiedTrue("newuser"))
                .thenReturn(false);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrue("existingemail@gmail.com"))
                .thenReturn(true);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(dto);
        });
        assertEquals("Email already exists", exception.getMessage());
    }

    // Register user - role "Users" not found
    @Test
    void register_roleUsersNotFound() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("anotheruser");
        dto.setEmail("phamnhan@gmail.com");
        dto.setPassword("SomePass@123");
        dto.setFullName("Another User");
        Mockito.when(userJpaRepository.existsByUsernameAndEmailVerifiedTrue("anotheruser"))
                .thenReturn(false);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrue("phamnhan@gmail.com"))
                .thenReturn(false);
        Mockito.when(userJpaRepository.findByEmail("phamnhan@gmail.com"))
                .thenReturn(Optional.empty());
        Mockito.when(roleJpaRepository.findByName("Users"))
                .thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(dto);
        });
        assertEquals("Role USER not found", exception.getMessage());
    }

    // Register user - email exists but not verified
    @Test
    void register_emailExistsNotVerified() throws Exception {
        // given
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("nhan");
        dto.setEmail("existingemail@gmail.com");
        dto.setPassword("SomePass@123");
        dto.setFullName("Nhan Existing");
        Mockito.when(userJpaRepository.existsByUsernameAndEmailVerifiedTrue("nhan"))
                .thenReturn(false);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrue("existingemail@gmail.com"))
                .thenReturn(false);
        User existingUser = new User();
        existingUser.setEmail("existingemail@gmail.com");
        Mockito.when(userJpaRepository.findByEmail("existingemail@gmail.com"))
                .thenReturn(Optional.of(existingUser));
        Role role = new Role();
        role.setName("Users");
        Mockito.when(roleJpaRepository.findByName("Users"))
                .thenReturn(Optional.of(role));
        // when
        RegisterResponseDto response = authService.register(dto);
        // then
        assertTrue(response.isSuccess());
        assertEquals("Registration successful. Please check your email for the verification code.",
                response.getMessage());
        verify(userJpaRepository).save(any(User.class));
        verify(emailService).sendVerificationCode(
                eq("existingemail@gmail.com"),
                anyString());
    }

    // register user - role "Users" not found
    @Test
    void register_emailExistsNotVerified_roleUsersNotFound() {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setUsername("nhan");
        dto.setEmail("phamnhan@gmail.com");
        dto.setPassword("SomePass@123");
        dto.setFullName("Nhan Pham");
        Mockito.when(userJpaRepository.existsByUsernameAndEmailVerifiedTrue("nhan"))
                .thenReturn(false);
        Mockito.when(userJpaRepository.existsByEmailAndEmailVerifiedTrue("phamnhan@gmail.com"))
                .thenReturn(false);
        User existingUser = new User();
        existingUser.setEmail("phamnhan@gmail.com");
        Mockito.when(userJpaRepository.findByEmail("phamnhan@gmail.com"))
                .thenReturn(Optional.of(existingUser));
        Mockito.when(roleJpaRepository.findByName("Users"))
                .thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(dto);
        });
        assertEquals("Role USER not found", exception.getMessage());
    }
    // ========= END REGISTER CREDENTIALS TESTS ==========

    // ========= Generate Random OTP TESTS ==========
    @Test
    void generateRandomOtp_success() {
        String otp = authService.generateOtp();
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("^[a-zA-Z0-9]+$"));
    }
    // ========= END Generate Random OTP TESTS ==========

    // ========= Verify Email with OTP TESTS ==========
    // verify email success
    @Test
    void verifyEmail_success() {
        User user = new User();
        user.setEmailVerified(false);
        user.setEmailOtpExpiry(System.currentTimeMillis() + 60_000);
        user.setEmailOtpHash("hashed-otp");

        Mockito.when(userJpaRepository.findByEmail("a@gmail.com"))
                .thenReturn(Optional.of(user));

        Mockito.when(passwordEncoder.matches("123456", "hashed-otp"))
                .thenReturn(true);

        // when
        authService.verifyEmail("a@gmail.com", "123456");

        // then
        assertTrue(user.isEmailVerified());
        assertNull(user.getEmailOtpHash());
        assertNull(user.getEmailOtpExpiry());

        verify(userJpaRepository).save(user);
    }

    // verify email - user not found
    @Test
    void verifyEmail_userNotFound() {
        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.empty());
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyEmail("nhan@gmail.com", "123456");
        });
        assertEquals("User not found", exception.getMessage());
    }

    // verify email - otp expired
    @Test
    void verifyEmail_otpExpired() {
        User user = new User();
        user.setEmailVerified(false);
        user.setEmailOtpExpiry(System.currentTimeMillis() - 1000); // expired
        user.setEmailOtpHash("hashed-otp");

        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyEmail("nhan@gmail.com", "123456");
        });
        assertEquals("OTP has expired", exception.getMessage());
    }

    // verify email - invalid otp
    @Test
    void verifyEmail_invalidOtp() {
        User user = new User();
        user.setEmailVerified(false);
        user.setEmailOtpExpiry(System.currentTimeMillis() + 60_000);
        user.setEmailOtpHash("hashed-otp");

        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("wrong-otp", "hashed-otp"))
                .thenReturn(false);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyEmail("nhan@gmail.com", "wrong-otp");
        });
        assertEquals("Invalid OTP", exception.getMessage());
    }
    // verify email - email already verified
    @Test
    void verifyEmail_emailAlreadyVerified() {
        User user = new User();
        user.setEmailVerified(true); // already verified

        Mockito.when(userJpaRepository.findByEmail("nhan@gmail.com"))
                .thenReturn(Optional.of(user));
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.verifyEmail("nhan@gmail.com", "123456");
        });
        assertEquals("Email is already verified", exception.getMessage());
    }
    // ========= END Verify Email with OTP TESTS ==========


    
}