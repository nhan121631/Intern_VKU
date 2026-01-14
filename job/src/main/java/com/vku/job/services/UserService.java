package com.vku.job.services;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vku.job.dtos.PaginatedResponseDto;
import com.vku.job.dtos.auth.GoogleLoginRequestDto;
import com.vku.job.dtos.auth.LoginRequestDto;
import com.vku.job.dtos.auth.LoginResponseDto;
import com.vku.job.dtos.auth.RegisterRequestDto;
import com.vku.job.dtos.auth.RegisterResponseDto;
import com.vku.job.dtos.user.FullNameUserResponse;
import com.vku.job.dtos.user.NameUserResponse;
import com.vku.job.dtos.user.UserResponse;
import com.vku.job.entities.Role;
import com.vku.job.entities.User;
import com.vku.job.entities.UserProfile;
import com.vku.job.exceptions.HttpException;
import com.vku.job.repositories.RoleJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.repositories.projection.FullNameUserProjection;
import com.vku.job.services.auth.JwtService;

import jakarta.transaction.Transactional;

@Service
public class UserService {
        @Autowired
        private UserJpaRepository userJpaRepository;

        @Autowired
        private JwtService jwtService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private RoleJpaRepository roleJpaRepository;

        @Autowired
        private MailService emailService;

        private final RestTemplate restTemplate = new RestTemplate();

        private static final SecureRandom secureRandom = new SecureRandom();

        // conver User entity to UserResponse dto
        private UserResponse convertToDto(User user) {
                UserResponse dto = new UserResponse();
                dto.setId(user.getId());
                dto.setUsername(user.getUsername());
                dto.setFullName(user.getProfile().getFullName());
                dto.setCreatedAt(user.getCreatedAt());
                dto.setIsActive(user.getIsActive());
                return dto;
        }

        // login with username and password
        public LoginResponseDto login(LoginRequestDto request) throws Exception {
                User user = this.userJpaRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new HttpException("Invalid username or password",
                                                HttpStatus.UNAUTHORIZED));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new HttpException("Invalid username or password", HttpStatus.UNAUTHORIZED);
                }

                if (!user.isEmailVerified()) {
                        throw new HttpException("Email is not verified. Please verify your email before logging in.",
                                        HttpStatus.FORBIDDEN);
                }

                if (user.getIsActive() == 1) {
                        throw new HttpException("Your account is not active. Please contact support.",
                                        HttpStatus.FORBIDDEN);
                }

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                LoginResponseDto.UserInfo userInfo = LoginResponseDto.UserInfo.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .isActive(user.getIsActive())
                                .roles(user.getRoles() != null
                                                ? user.getRoles().stream().map(Role::getName).toList()
                                                : null)
                                .build();

                return LoginResponseDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .loggedInUser(userInfo)
                                .build();
        }

        // login with google
        @Transactional
        public LoginResponseDto googleLogin(GoogleLoginRequestDto requestDto) {
                String credential = requestDto.getCredential();
                String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + credential;
                @SuppressWarnings("rawtypes")
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = response.getBody();
                if (response.getStatusCode() != HttpStatus.OK) {
                        throw new HttpException("Invalid Google token", HttpStatus.UNAUTHORIZED);
                }

                String email;
                if (payload != null && payload.containsKey("email")) {
                        email = payload.get("email").toString();
                } else {
                        throw new HttpException("Email not found in token", HttpStatus.UNAUTHORIZED);
                }

                String iss = payload.get("iss").toString();
                if (!iss.equals("https://accounts.google.com") && !iss.equals("accounts.google.com")) {
                        throw new HttpException("Invalid Google token issuer", HttpStatus.UNAUTHORIZED);
                }

                long exp = Long.parseLong(payload.get("exp").toString());
                if (exp < System.currentTimeMillis() / 1000) {
                        throw new HttpException("Google token has expired", HttpStatus.UNAUTHORIZED);
                }
                // check if user exists

                if (userJpaRepository.existsByEmailAndEmailVerifiedTrueAndPasswordIsNotNull(email)) {
                        throw new HttpException(
                                        "Email already registered. Please login with your credentials.",
                                        HttpStatus.FORBIDDEN);
                }

                User user = userJpaRepository.findByUsername(email).orElse(null);
                if (user == null) {
                        // create new user
                        user = new User();
                        user.setUsername(email);
                        user.setEmail(email);
                        user.setEmailVerified(true);
                        user.setIsActive(0);
                        user.setPassword(null);
                        UserProfile profile = new UserProfile();
                        profile.setFullName(payload.get("name").toString());
                        profile.setUser(user);
                        user.setProfile(profile);
                        Role userRole = roleJpaRepository.findByName("Users").orElseThrow();
                        user.setRoles(List.of(userRole));
                        userJpaRepository.save(user);
                }

                if (user.getIsActive() == 1) {
                        throw new HttpException("Your account is not active. Please contact support.",
                                        HttpStatus.FORBIDDEN);
                }

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);
                LoginResponseDto.UserInfo userInfo = LoginResponseDto.UserInfo.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .isActive(user.getIsActive())
                                .roles(user.getRoles() != null
                                                ? user.getRoles().stream().map(Role::getName).toList()
                                                : null)
                                .build();
                return LoginResponseDto.builder()
                                .loggedInUser(userInfo)
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .build();

        }

        // generate otp for email verification
        private String generateOtp() {
                int otp = secureRandom.nextInt(900000) + 100000;
                return String.valueOf(otp);
        }

        // register new user
        @Transactional
        public RegisterResponseDto register(RegisterRequestDto request) throws Exception {

                // Check username/email đã verified
                if (userJpaRepository.existsByUsernameAndEmailVerifiedTrue(request.getUsername())) {
                        throw new IllegalArgumentException("Username already exists");
                }
                if (userJpaRepository.existsByEmailAndEmailVerifiedTrue(request.getEmail())) {
                        throw new IllegalArgumentException("Email already exists");
                }

                User user = userJpaRepository.findByEmail(request.getEmail())
                                .orElseGet(User::new);

                boolean isNewUser = user.getId() == null;

                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setEmailVerified(false);
                user.setIsActive(0);

                if (isNewUser) {
                        Role userRole = roleJpaRepository.findByName("Users")
                                        .orElseThrow(() -> new RuntimeException("Role USER not found"));
                        user.setRoles(List.of(userRole));
                }

                UserProfile profile = user.getProfile();
                if (profile == null) {
                        profile = new UserProfile();
                        profile.setUser(user);
                        user.setProfile(profile);
                }
                profile.setFullName(request.getFullName());

                String otp = generateOtp();
                user.setEmailOtpHash(passwordEncoder.encode(otp));
                user.setEmailOtpExpiry(System.currentTimeMillis() + 1 * 60 * 1000); // 1 minute expiry

                userJpaRepository.save(user);

                emailService.sendVerificationCode(request.getEmail(), otp);

                RegisterResponseDto response = new RegisterResponseDto();
                response.setSuccess(true);
                response.setMessage("Registration successful. Please check your email for the verification code.");
                response.setEmail(request.getEmail());
                return response;
        }

        public void verifyEmail(String email, String otp) {
                User user = userJpaRepository.findByEmail(email)
                                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));

                if (user.isEmailVerified()) {
                        throw new HttpException("Email is already verified", HttpStatus.BAD_REQUEST);
                }

                if (user.getEmailOtpExpiry() == null || user.getEmailOtpExpiry() < System.currentTimeMillis()) {
                        throw new HttpException("OTP has expired", HttpStatus.BAD_REQUEST);
                }

                if (!passwordEncoder.matches(otp, user.getEmailOtpHash())) {
                        throw new HttpException("Invalid OTP", HttpStatus.BAD_REQUEST);
                }

                user.setEmailVerified(true);
                user.setEmailOtpHash(null);
                user.setEmailOtpExpiry(null);
                userJpaRepository.save(user);
        }

        // get Fullname all users
        public List<FullNameUserResponse> getAllFullNameUsers() {
                List<FullNameUserProjection> projections = userJpaRepository.getAllFullNameUser();

                return projections.stream().map(proj -> {
                        FullNameUserResponse response = new FullNameUserResponse();
                        response.setId(proj.getId());
                        response.setFullName(proj.getFullName());
                        return response;
                }).toList();
        }

        // get all users panigated
        public PaginatedResponseDto<UserResponse> getAllUsersPaginated(int page,
                        int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<User> users = userJpaRepository.findAll(pageable);
                PaginatedResponseDto<UserResponse> response = new PaginatedResponseDto<>(
                                users.map(this::convertToDto).getContent(),
                                users.getNumber(),
                                users.getSize(),
                                users.getTotalElements(),
                                users.getTotalPages(),
                                users.hasNext(),
                                users.hasPrevious());
                return response;
        }

        // change user status
        public void changeUserStatus(Long userId, int isActive) {
                User user = userJpaRepository.findById(userId)
                                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));
                if (isActive != 0 && isActive != 1) {
                        throw new HttpException("Invalid status value", HttpStatus.BAD_REQUEST);
                }
                user.setIsActive(isActive);
                userJpaRepository.save(user);
        }

        // fetch name user
        public NameUserResponse getNameUserById(Long userId) {
                User user = userJpaRepository.findById(userId)
                                .orElseThrow(() -> new HttpException("User not found", HttpStatus.NOT_FOUND));
                NameUserResponse response = new NameUserResponse();
                response.setFullName(user.getProfile().getFullName());
                return response;
        }
}
