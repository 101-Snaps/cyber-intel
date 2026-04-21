package com.nicasia.cyberintel;

import com.nicasia.cyberintel.filter.JwtFilter;
import com.nicasia.cyberintel.service.UserService;          // ADD
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;  // ADD
import org.springframework.security.authentication.AuthenticationProvider; // ADD
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // ADD
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // ADD
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final UserService userService;  // ADD

    public SecurityConfig(JwtFilter jwtFilter, UserService userService) {  // ADD
        this.jwtFilter = jwtFilter;
        this.userService = userService;  // ADD
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ADD THIS — wires UserService + PasswordEncoder into Spring Security
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ADD THIS — exposes AuthenticationManager for use in controllers if needed
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
            "https://101-snaps.github.io"
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
            .authenticationProvider(authenticationProvider())  // ADD THIS LINE
            .authorizeHttpRequests(auth -> auth
                // FIX: update this to match your actual controller path
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
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
