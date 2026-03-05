package com.littlek4za.booking_system.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlek4za.booking_system.dtos.ErrorResponseDto;
import com.littlek4za.booking_system.exception.filter.ExceptionHandlerFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SecurityConfig {

    @Value("${allowed.origins}")
    private String[] allowedOrigins;
    private final UserAuthProvider userAuthProvider;

    public SecurityConfig(UserAuthProvider userAuthProvider) {
        this.userAuthProvider = userAuthProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .addFilterBefore(new JwtAuthFilter(userAuthProvider), BasicAuthenticationFilter.class)
                .addFilterBefore(new ExceptionHandlerFilter(objectMapper), JwtAuthFilter.class)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        request -> request
                        .requestMatchers(HttpMethod.POST, "/api/v1/login", "/api/v1/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/invitations/*/validate").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            log.warn("authenticationEntryPoint failed: {}", e.getMessage());
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            ErrorResponseDto errorResponseDto = ErrorResponseDto.create(
                                HttpStatus.UNAUTHORIZED, 
                                "Token missing or invalid", 
                                request.getServletPath(),
                                null);
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponseDto));
                        }))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .cors(cors -> {
                });

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}
