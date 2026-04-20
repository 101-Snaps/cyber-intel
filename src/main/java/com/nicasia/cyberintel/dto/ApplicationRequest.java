package com.nicasia.cyberintel.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApplicationRequest {
    private String fullName;
    private String email;
    private String phone;
    private String gender;
    private String nationality;
    private String idNumber;
    private Boolean hasDisability;
    private String disabilityDetails;
    private String jobTitle;
    private String jobCompany;
    private String jobLocation;
    private List<EducationDto> educationList;
    private List<ExperienceDto> experienceList;
}