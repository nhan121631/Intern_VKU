package com.vku.job.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.vku.job.exceptions.CustomAccessDeniedHandler;
import com.vku.job.exceptions.CustomAuthenticationEntryPoint;
import com.vku.job.filters.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
@RequiredArgsConstructor
public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.cors(withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(this.customAuthenticationEntryPoint)
                                                .accessDeniedHandler(this.customAccessDeniedHandler))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/uploads/**").permitAll()
                                                .requestMatchers("/swagger-ui.html*").permitAll()
                                                .requestMatchers("/swagger-ui/**").permitAll()
                                                .requestMatchers("/v3/api-docs/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/tasks")
                                                .hasAllRoles("Administrators")
                                                .requestMatchers(HttpMethod.GET, "/api/tasks")
                                                .hasAllRoles("Administrators")
                                                .requestMatchers(HttpMethod.PATCH, "/api/tasks")
                                                .hasAnyRole("Administrators")
                                                .requestMatchers("/api/tasks/export")
                                                .hasAnyRole("Administrators")
                                                .requestMatchers("/api/tasks/search-by-title")
                                                .hasAnyRole("Administrators")
                                                .requestMatchers("/api/tasks/filter-by-status")
                                                .hasAnyRole("Administrators")
                                                .requestMatchers("/api/users/get-name-by-id")
                                                .hasAnyRole("Administrators", "Users")
                                                .requestMatchers("/api/users/get-profile")
                                                .hasAnyRole("Administrators", "Users")
                                                .requestMatchers("/api/users/update-profile")
                                                .hasAnyRole("Administrators", "Users")
                                                .requestMatchers("/api/users/change-password")
                                                .hasAnyRole("Administrators", "Users")
                                                .requestMatchers("/api/users/**")
                                                .hasAnyRole("Administrators")
                                                .requestMatchers("/api/task-histories/**")
                                                .hasAnyRole("Administrators")
                                                .requestMatchers("/api/statistics/**")
                                                .hasAnyRole("Administrators")
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(false); // Không cho gửi cookie/token
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}