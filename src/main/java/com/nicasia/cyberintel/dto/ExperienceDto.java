package com.nicasia.cyberintel.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExperienceDto {
    private String company;
    private String role;
    private Integer years;
    private String description;
}