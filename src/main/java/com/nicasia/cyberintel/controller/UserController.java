package com.nicasia.cyberintel.controller;

import com.nicasia.cyberintel.model.User;
import com.nicasia.cyberintel.service.SecurityMonitorService;
import com.nicasia.cyberintel.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;
    private final SecurityMonitorService securityMonitorService;

    public UserController(UserService userService,
                          SecurityMonitorService securityMonitorService) {
        this.userService = userService;
        this.securityMonitorService = securityMonitorService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user,
                                       HttpServletRequest request) {
        try {
            User registered = userService.register(user);
            securityMonitorService.log(
                user.getEmail(), "REGISTER", "/api/auth/register",
                "SUCCESS", request.getRemoteAddr(), "New user registered"
            );
            return ResponseEntity.ok(registered);
        } catch (RuntimeException e) {
            securityMonitorService.log(
                user.getEmail(), "REGISTER", "/api/auth/register",
                "FAILED", request.getRemoteAddr(), e.getMessage()
            );
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    // ✅ Updated — now accepts selectedRole in request body
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                    HttpServletRequest request) {
        try {
            // Extract fields from request body
            User user = new User();
            user.setEmail(body.get("email"));
            user.setPassword(body.get("password"));

            // selectedRole comes from Angular login toggle
            String selectedRole = body.getOrDefault("selectedRole", "USER");
            // Normalize: Angular sends 'applicant' or 'staff'
            String expectedRole = "staff".equalsIgnoreCase(selectedRole) ? "STAFF" : "USER";

            Object result = userService.login(user, expectedRole);

            securityMonitorService.log(
                user.getEmail(), "LOGIN", "/api/auth/login",
                "SUCCESS", request.getRemoteAddr(), "Login successful"
            );

            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            securityMonitorService.log(
                body.get("email"), "LOGIN", "/api/auth/login",
                "FAILED", request.getRemoteAddr(), e.getMessage()
            );
            securityMonitorService.checkBruteForce(
                request.getRemoteAddr(), body.get("email")
            );
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
