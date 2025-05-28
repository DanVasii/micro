// inventory-service/src/main/java/com/footwear/inventoryservice/config/SecurityConfig.java
package com.footwear.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to these endpoints
                        .requestMatchers("/api/inventory/**").permitAll()
                        .requestMatchers("/api/inventory/health").permitAll()
                        .requestMatchers("/api/inventory/test").permitAll()
                        .requestMatchers("/api/inventory/debug/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                // Disable form login and use stateless authentication
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}