package com.mancy.quizplatform.configuration;

import com.mancy.quizplatform.security.RoleBasedSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Authentication = "who are you?" (login). Authorization = "what may you open?" (the URL rules below).
 * CSRF protection stays ON: every POST form carries a hidden token (Thymeleaf adds it with th:action).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** BCrypt turns a password into a one-way hash with a random salt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RoleBasedSuccessHandler successHandler)
            throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/vendor/**", "/images/**").permitAll()
                .requestMatchers("/", "/login", "/register", "/access-denied", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/creator/**").hasRole("CREATOR")
                .requestMatchers("/participant/**").hasRole("PARTICIPANT")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")                 // POST only, protected by the CSRF token
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));
        return http.build();
    }
}
