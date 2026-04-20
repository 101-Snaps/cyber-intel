package com.nicasia.cyberintel.service;

import com.nicasia.cyberintel.dto.*;
import com.nicasia.cyberintel.model.*;
import com.nicasia.cyberintel.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    // Folder where uploaded files are saved — create this folder on your machine
    private final String uploadDir = "uploads/";

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application saveApplication(
            ApplicationRequest request,
            MultipartFile cvFile,
            MultipartFile idFile
    ) throws IOException {

        Application app = new Application();

        // Personal info
        app.setFullName(request.getFullName());
        app.setEmail(request.getEmail());
        app.setPhone(request.getPhone());
        app.setGender(request.getGender());
        app.setNationality(request.getNationality());
        app.setIdNumber(request.getIdNumber());
        app.setHasDisability(request.getHasDisability());
        app.setDisabilityDetails(request.getDisabilityDetails());

        // Job info
        app.setJobTitle(request.getJobTitle());
        app.setJobCompany(request.getJobCompany());
        app.setJobLocation(request.getJobLocation());

        // Save CV file
        if (cvFile != null && !cvFile.isEmpty()) {
            app.setCvPath(saveFile(cvFile));
        }

        // Save ID file
        if (idFile != null && !idFile.isEmpty()) {
            app.setIdDocPath(saveFile(idFile));
        }

        // Map education
        if (request.getEducationList() != null) {
            List<Education> eduList = request.getEducationList().stream().map(dto -> {
                Education edu = new Education();
                edu.setQualification(dto.getQualification());
                edu.setInstitution(dto.getInstitution());
                edu.setYear(dto.getYear());
                edu.setApplication(app);
                return edu;
            }).collect(Collectors.toList());
            app.setEducationList(eduList);
        }

        // Map experience
        if (request.getExperienceList() != null) {
            List<Experience> expList = request.getExperienceList().stream().map(dto -> {
                Experience exp = new Experience();
                exp.setCompany(dto.getCompany());
                exp.setRole(dto.getRole());
                exp.setYears(dto.getYears());
                exp.setDescription(dto.getDescription());
                exp.setApplication(app);
                return exp;
            }).collect(Collectors.toList());
            app.setExperienceList(expList);
        }

        return applicationRepository.save(app);
    }

    private String saveFile(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(uploadDir));
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir + filename);
        Files.write(path, file.getBytes());
        return path.toString();
    }
}