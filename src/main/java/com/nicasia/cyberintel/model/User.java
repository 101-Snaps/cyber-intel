package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_role",  columnList = "role")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String surname;
    private String cell;

    @Email(message = "Valid email required")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;  // USER, STAFF, ADMIN

    private Integer loginCount = 0;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    @Transient
    private String staffCode;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.loginCount == null) this.loginCount = 0;
        if (this.role == null) this.role = "USER";
    }
}
