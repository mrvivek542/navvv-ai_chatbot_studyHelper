package com.studymind.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig provides enterprise-grade Spring Security hardening:
 * 1. Role-Based Access Control (RBAC): Admin routes & H2 Console restricted to ROLE_ADMIN.
 * 2. Strong Password Hashing: BCryptPasswordEncoder (12 rounds).
 * 3. Security Headers: CSP, Frame-Options, X-Content-Type-Options, Referrer-Policy.
 * 4. CORS Whitelisting: Strict localhost origin restrictions.
 * 5. CSRF defense for sensitive operations.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.admin-password:${APP_ADMIN_PASSWORD:Admin@StudyMind2026!}}")
    private String adminPassword;

    @Value("${app.security.user-password:${APP_USER_PASSWORD:Student@StudyMind2026!}}")
    private String userPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN", "USER")
                .build();

        UserDetails studentUser = User.builder()
                .username("student")
                .password(passwordEncoder.encode(userPassword))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(adminUser, studentUser);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://127.0.0.1:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 2. CSRF (Exempt stateless API and H2 Console while securing standard endpoints)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/h2-console/**"))

            // 3. Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Restrict Admin routes and H2 Console strictly to ROLE_ADMIN
                .requestMatchers("/h2-console/**", "/admin/**").hasRole("ADMIN")
                // Publicly accessible application routes and assets
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/error",
                    "/api/**"
                ).permitAll()
                // All other routes require authentication
                .anyRequest().authenticated()
            )

            // 4. Form Login & HTTP Basic for Admin access
            .formLogin(form -> form.permitAll())
            .httpBasic(basic -> basic.realmName("StudyMind Admin Realm"))
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )

            // 5. Security Headers
            .headers(headers -> headers
                // SameOrigin prevents external clickjacking while allowing embedded H2 Console for Admin
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions(contentType -> {})
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; " +
                    "img-src 'self' data: https:; " +
                    "connect-src 'self'; " +
                    "frame-ancestors 'self';"
                ))
            );

        return http.build();
    }
}
