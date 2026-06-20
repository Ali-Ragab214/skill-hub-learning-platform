package com.example.Skill_Hub_Learning_Platform.infrastructure.security;

import com.example.Skill_Hub_Learning_Platform.application.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/instructor/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/courses/**").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/courses/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/courses/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/courses/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/sections").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/sections/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/sections/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/courses/*/sections").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/*/sections/**").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/*/sections/**").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/sections/*/lessons/preview").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/sections/*/lessons").hasAnyRole("STUDENT", "INSTRUCTOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/sections/*/lessons/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/courses/*/sections/*/lessons").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/*/sections/*/lessons/**").hasRole("INSTRUCTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/*/sections/*/lessons/**").hasRole("INSTRUCTOR")

                        .requestMatchers(HttpMethod.POST, "/api/lessons/*/progress").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/lessons/*/progress").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/lessons/*/progress").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/progress").hasRole("STUDENT")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
