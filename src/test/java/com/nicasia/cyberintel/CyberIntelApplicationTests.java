package com.nicasia.cyberintel;

import com.nicasia.cyberintel.exception.ResourceNotFoundException;
import com.nicasia.cyberintel.model.Incident;
import com.nicasia.cyberintel.model.Threat;
import com.nicasia.cyberintel.model.User;
import com.nicasia.cyberintel.repository.IncidentRepository;
import com.nicasia.cyberintel.repository.UserRepository;
import com.nicasia.cyberintel.service.AiService;
import com.nicasia.cyberintel.service.IncidentService;
import com.nicasia.cyberintel.service.ThreatService;
import com.nicasia.cyberintel.service.UserService;
import com.nicasia.cyberintel.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CyberIntelApplicationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private IncidentService incidentService;
    @Autowired private ThreatService threatService;
    @Autowired private UserService userService;
    @Autowired private IncidentRepository incidentRepository;
    // threatRepository removed — it was injected but never directly used in any test;
    // ThreatService already has it internally and is tested via the service.
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @MockBean
    private AiService aiService;

    private String staffToken;

    @BeforeEach
    void setUp() {
        when(aiService.predict(anyString(), anyString(), anyInt()))
            .thenReturn(new AiService.AiPrediction("HIGH", 0.87));
        when(aiService.predictRiskLevel(anyString(), anyString(), anyInt()))
            .thenReturn("HIGH");

        if (userRepository.findByEmail("test-staff@nicasia.com").isEmpty()) {
            User staff = new User();
            staff.setName("Test");
            staff.setSurname("Staff");
            staff.setEmail("test-staff@nicasia.com");
            staff.setPassword(passwordEncoder.encode("testpass123"));
            staff.setRole("STAFF");
            userRepository.save(staff);
        }
        staffToken = jwtUtil.generateToken("test-staff@nicasia.com", "STAFF");
    }

    // ── Context ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
        assertThat(incidentService).isNotNull();
        assertThat(threatService).isNotNull();
        assertThat(userService).isNotNull();
    }

    // ── Incident service ─────────────────────────────────────────────────

    @Test
    @DisplayName("Create incident — persists and returns id + createdAt")
    void createIncident_success() {
        Incident inc = new Incident();
        inc.setTitle("Test Ransomware Attack");
        inc.setSeverity("HIGH");
        inc.setType("Ransomware");

        Incident saved = incidentService.create(inc);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test Ransomware Attack");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("Get incident by id — not found throws ResourceNotFoundException")
    void getIncidentById_notFound_throws() {
        assertThatThrownBy(() -> incidentService.getById(999999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("999999");
    }

    @Test
    @DisplayName("Update incident — changes are persisted")
    void updateIncident_success() {
        Incident inc = new Incident();
        inc.setTitle("Original Title");
        inc.setSeverity("LOW");
        inc.setType("Phishing");
        Incident saved = incidentService.create(inc);

        Incident update = new Incident();
        update.setTitle("Updated Title");
        update.setSeverity("CRITICAL");
        update.setType("Ransomware");
        update.setStatus("IN_PROGRESS");

        // Objects.requireNonNull satisfies @NonNull on the id parameter —
        // suppresses the "Long needs unchecked conversion" null-safety warning.
        Incident updated = incidentService.update(Objects.requireNonNull(saved.getId()), update);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getSeverity()).isEqualTo("CRITICAL");
    }

    @Test
    @DisplayName("Delete incident — removed from repository")
    void deleteIncident_success() {
        Incident inc = new Incident();
        inc.setTitle("To Delete");
        inc.setSeverity("LOW");
        inc.setType("Virus");
        Incident saved = incidentService.create(inc);
        Long id = Objects.requireNonNull(saved.getId());

        incidentService.delete(id);

        assertThat(incidentRepository.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("Delete incident — not found throws ResourceNotFoundException")
    void deleteIncident_notFound_throws() {
        assertThatThrownBy(() -> incidentService.delete(999999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Incident stats — returns map with expected keys")
    void incidentStats_returnsCorrectKeys() {
        Incident inc = new Incident();
        inc.setTitle("Stat Incident");
        inc.setSeverity("HIGH");
        inc.setType("DDoS");
        incidentService.create(inc);

        var stats = incidentService.getStats();

        assertThat(stats).containsKeys("total", "open", "inProgress", "resolved", "critical", "high");
        assertThat(stats.get("total")).isGreaterThan(0L);
    }

    // ── Threat service ───────────────────────────────────────────────────

    @Test
    @DisplayName("Create threat — AI risk level and confidence are stored")
    void createThreat_aiRiskSet() {
        Threat t = new Threat();
        t.setName("ExploitKit-X");
        t.setCategory("Ransomware");
        t.setSource("External");

        Threat saved = threatService.create(t);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRiskLevel()).isEqualTo("HIGH");
        assertThat(saved.getAiConfidence()).isEqualTo(0.87);
    }

    @Test
    @DisplayName("Delete threat — not found throws ResourceNotFoundException")
    void deleteThreat_notFound_throws() {
        assertThatThrownBy(() -> threatService.delete(999999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── User service ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Register user — password is BCrypt hashed")
    void registerUser_passwordHashed() {
        User user = new User();
        user.setName("Jane");
        user.setSurname("Doe");
        user.setEmail("jane.doe.warn@example.com");
        user.setPassword("plainpassword");
        user.setRole("USER");

        User registered = userService.register(user);

        assertThat(registered.getPassword()).isNotEqualTo("plainpassword");
        assertThat(passwordEncoder.matches("plainpassword", registered.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Register user — duplicate email throws")
    void registerUser_duplicateEmail_throws() {
        User u1 = new User();
        u1.setName("First");
        u1.setEmail("dup.warn@example.com");
        u1.setPassword("pass1");
        u1.setRole("USER");
        userService.register(u1);

        User u2 = new User();
        u2.setName("Second");
        u2.setEmail("dup.warn@example.com");
        u2.setPassword("pass2");
        u2.setRole("USER");

        assertThatThrownBy(() -> userService.register(u2))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("Login — wrong password throws")
    void login_wrongPassword_throws() {
        User user = new User();
        user.setName("Login");
        user.setEmail("login.warn@example.com");
        user.setPassword("correctpassword");
        user.setRole("USER");
        userService.register(user);

        User attempt = new User();
        attempt.setEmail("login.warn@example.com");
        attempt.setPassword("wrongpassword");

        assertThatThrownBy(() -> userService.login(attempt, "USER"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid password");
    }

    // ── REST API integration ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/incidents — 403 without token")
    void getIncidents_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/incidents"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/incidents — 200 with valid staff JWT")
    void getIncidents_withStaffJwt_returns200() throws Exception {
        mockMvc.perform(get("/api/incidents")
                .header("Authorization", "Bearer " + staffToken))
            .andExpect(status().isOk())
            // Objects.requireNonNull satisfies the @NonNull contract on
            // contentType(), eliminating the MediaType null-safety warning.
            .andExpect(content().contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON)));
    }

    @Test
    @DisplayName("POST /api/incidents — creates incident and returns id")
    void postIncident_creates() throws Exception {
        String json = """
            {
              "title": "API Test Incident",
              "severity": "HIGH",
              "type": "DDoS",
              "status": "OPEN"
            }
            """;

        mockMvc.perform(post("/api/incidents")
                .header("Authorization", "Bearer " + staffToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("API Test Incident"))
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register — public endpoint (no token needed)")
    void register_isPublic() throws Exception {
        String json = """
            {
              "name": "Public",
              "surname": "User",
              "email": "public.warn.test@example.com",
              "password": "securepass",
              "role": "USER"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(json))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/incidents/999999 — returns 404 with error body")
    void deleteNonExistentIncident_returns404() throws Exception {
        mockMvc.perform(delete("/api/incidents/999999")
                .header("Authorization", "Bearer " + staffToken))
            .andExpect(status().isNotFound());
    }

    // ── JWT utility ──────────────────────────────────────────────────────

    @Test
    @DisplayName("JWT — generated token passes validation")
    void jwt_generatedToken_isValid() {
        String token = jwtUtil.generateToken("test@example.com", "STAFF");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("test@example.com");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("STAFF");
    }

    @Test
    @DisplayName("JWT — tampered token fails validation")
    void jwt_tamperedToken_isInvalid() {
        String token = jwtUtil.generateToken("test@example.com", "STAFF");
        assertThat(jwtUtil.isTokenValid(token + "tampered")).isFalse();
    }
}
