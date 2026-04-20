package com.nicasia.cyberintel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "education")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String qualification;
    private String institution;
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;
}