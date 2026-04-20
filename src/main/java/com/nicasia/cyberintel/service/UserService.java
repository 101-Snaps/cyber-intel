package com.nicasia.cyberintel.service;

import com.nicasia.cyberintel.model.User;
import com.nicasia.cyberintel.repository.UserRepository;
import com.nicasia.cyberintel.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private static final String STAFF_SECRET_CODE = "NICASIA-STAFF-2024";

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTER
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if ("STAFF".equals(user.getRole())) {
            if (user.getStaffCode() == null ||
                !STAFF_SECRET_CODE.equals(user.getStaffCode().trim())) {
                throw new RuntimeException("Invalid staff registration code");
            }
        } else {
            // Force all non-staff registrations to USER — prevents privilege escalation
            user.setRole("USER");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStaffCode(null);
        return userRepository.save(user);
    }

    // LOGIN
    public Object login(User user, String expectedRole) {
        return userRepository.findByEmail(user.getEmail())
            .map(existingUser -> {

                if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                    throw new RuntimeException("Incorrect password. Please try again.");
                }

                String actualRole = existingUser.getRole();

                // FIX Bug 3: ADMIN must also be treated as the staff side,
                // otherwise ADMIN accounts get a wrong-role error on the Staff tab.
                boolean isStaffSide = "STAFF".equals(actualRole) || "ADMIN".equals(actualRole);

                if ("STAFF".equals(expectedRole) && !isStaffSide) {
                    throw new RuntimeException(
                        "This is an applicant account. Please use the Applicant tab.");
                }

                if ("USER".equals(expectedRole) && isStaffSide) {
                    throw new RuntimeException(
                        "This is a staff account. Please use the Staff / Admin tab.");
                }

                existingUser.setLoginCount(
                    existingUser.getLoginCount() == null ? 1
                    : existingUser.getLoginCount() + 1
                );
                existingUser.setLastLogin(LocalDateTime.now());
                userRepository.save(existingUser);

                String token = jwtUtil.generateToken(
                    existingUser.getEmail(),
                    existingUser.getRole()
                );

                return Map.of(
                    "message", "Login successful",
                    "token", token,
                    "user", Map.of(
                        "id",    existingUser.getId(),
                        "name",  existingUser.getName(),
                        "email", existingUser.getEmail(),
                        "role",  existingUser.getRole()
                    )
                );
            })
            .orElseThrow(() -> new RuntimeException("No account found with that email address."));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
