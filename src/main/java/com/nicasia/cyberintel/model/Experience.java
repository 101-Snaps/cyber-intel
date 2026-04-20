package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experience")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;
    private String role;
    private Integer years;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}