package com.nicasia.cyberintel.controller;

import com.nicasia.cyberintel.dto.ApplicationRequest;
import com.nicasia.cyberintel.model.Application;
import com.nicasia.cyberintel.service.ApplicationService;
import com.nicasia.cyberintel.service.FileScanService;
import com.nicasia.cyberintel.service.SecurityMonitorService;
import com.nicasia.cyberintel.util.InputSanitizer;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final FileScanService fileScanService;
    private final SecurityMonitorService securityMonitorService;
    private final InputSanitizer inputSanitizer;

    public ApplicationController(
            ApplicationService applicationService,
            FileScanService fileScanService,
            SecurityMonitorService securityMonitorService,
            InputSanitizer inputSanitizer
    ) {
        this.applicationService = applicationService;
        this.fileScanService = fileScanService;
        this.securityMonitorService = securityMonitorService;
        this.inputSanitizer = inputSanitizer;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> submitApplication(
            @RequestPart("data") ApplicationRequest request,
            @RequestPart(value = "cv", required = false) MultipartFile cvFile,
            @RequestPart(value = "id", required = false) MultipartFile idFile,
            HttpServletRequest httpRequest
    ) {
        try {
            String ip = httpRequest.getRemoteAddr();
            String email = request.getEmail();

            // ✅ Sanitize inputs
            request.setFullName(inputSanitizer.sanitize(request.getFullName()));
            request.setEmail(inputSanitizer.sanitize(request.getEmail()));
            request.setPhone(inputSanitizer.sanitize(request.getPhone()));

            // ✅ Validate email
            if (!inputSanitizer.isValidEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid email format"));
            }

            // ✅ Scan CV file
            if (cvFile != null) {
                FileScanService.ScanResult cvScan = fileScanService.scan(cvFile);
                if (!cvScan.clean()) {
                    securityMonitorService.checkSuspiciousUpload(
                            email, ip, cvFile.getOriginalFilename(), cvFile.getSize());

                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "CV scan failed: " + cvScan.message()));
                }
            }

            // ✅ Scan ID file
            if (idFile != null) {
                FileScanService.ScanResult idScan = fileScanService.scan(idFile);
                if (!idScan.clean()) {
                    securityMonitorService.checkSuspiciousUpload(
                            email, ip, idFile.getOriginalFilename(), idFile.getSize());

                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "ID scan failed: " + idScan.message()));
                }
            }

            // ✅ Save application
            Application saved = applicationService.saveApplication(request, cvFile, idFile);

            // ✅ Log success
            securityMonitorService.log(
                    email,
                    "UPLOAD",
                    "/api/applications",
                    "SUCCESS",
                    ip,
                    "Application submitted with CV and ID"
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Application submitted successfully!",
                    "id", saved.getId()
            ));

        } catch (Exception e) {

            // ✅ Log failure
            securityMonitorService.log(
                    request.getEmail(),
                    "UPLOAD",
                    "/api/applications",
                    "FAILED",
                    httpRequest.getRemoteAddr(),
                    e.getMessage()
            );

            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}