package com.vku.job.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vku.job.dtos.auth.LoginRequestDto;
import com.vku.job.dtos.auth.LoginResponseDto;
import com.vku.job.dtos.auth.RegisterRequestDto;
import com.vku.job.entities.Role;
import com.vku.job.entities.User;
import com.vku.job.exceptions.HttpException;
import com.vku.job.repositories.RoleJpaRepository;
import com.vku.job.repositories.UserJpaRepository;
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

                return LoginResponseDto.builder()
                                .id(user.getId())
                                .fullName(user.getFullName())
                                .username(user.getUsername())
                                .roles(user.getRoles() != null
                                                ? user.getRoles().stream().map(Role::getName).toList()
                                                : null)
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

}
