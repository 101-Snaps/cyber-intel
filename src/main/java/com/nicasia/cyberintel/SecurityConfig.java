package com.nicasia.cyberintel;

import com.nicasia.cyberintel.filter.JwtFilter;
import com.nicasia.cyberintel.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder; // injected, not defined here

    public SecurityConfig(JwtFilter jwtFilter,
                          UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.jwtFilter = jwtFilter;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ REMOVED @Bean passwordEncoder() — it now lives in PasswordEncoderConfig

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:4200",
            "http://localhost:4201",
            "https://101-Snaps.github.io"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("/graphql", "/graphiql", "/graphiql/**").permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/incidents/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/threats/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/security/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/applications/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
