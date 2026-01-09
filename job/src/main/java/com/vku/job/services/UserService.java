package com.vku.job.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vku.job.dtos.User.FullNameUserResponse;
import com.vku.job.dtos.auth.GoogleLoginRequestDto;
import com.vku.job.dtos.auth.LoginRequestDto;
import com.vku.job.dtos.auth.LoginResponseDto;
import com.vku.job.dtos.auth.RegisterRequestDto;
import com.vku.job.entities.Role;
import com.vku.job.entities.User;
import com.vku.job.exceptions.HttpException;
import com.vku.job.repositories.RoleJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
import com.vku.job.repositories.projection.FullNameUserProjection;
import com.vku.job.services.auth.JwtService;

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

        private final RestTemplate restTemplate = new RestTemplate();

        // login with username and password
        public LoginResponseDto login(LoginRequestDto request) throws Exception {
                User user = this.userJpaRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new HttpException("Invalid username or password",
                                                HttpStatus.UNAUTHORIZED));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new HttpException("Invalid username or password", HttpStatus.UNAUTHORIZED);
                }

                if (user.getIsActive() == 1) {
                        throw new HttpException("Your account is not active. Please contact support.",
                                        HttpStatus.FORBIDDEN);
                }

                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                LoginResponseDto.UserInfo userInfo = LoginResponseDto.UserInfo.builder()
                                .id(user.getId())
                                .fullName(user.getFullName())
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
                User user = userJpaRepository.findByUsername(email).orElse(null);
                if (user == null) {
                        // create new user
                        user = new User();
                        user.setUsername(email);
                        user.setFullName(payload.get("name").toString());
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
                                .fullName(user.getFullName())
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

        // register new user
        public void register(RegisterRequestDto request) throws Exception {
                if (userJpaRepository.existsByUsername(request.getUsername())) {
                        throw new IllegalArgumentException("Username already exists");
                }

                User newUser = new User();
                newUser.setUsername(request.getUsername());
                Role userRole = roleJpaRepository.findByName("Users").orElseThrow();
                newUser.setRoles(List.of(userRole));
                newUser.setPassword(passwordEncoder.encode(request.getPassword()));
                newUser.setFullName(request.getFullName());
                userJpaRepository.save(newUser);

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

}
