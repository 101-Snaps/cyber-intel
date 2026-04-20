package com.nicasia.cyberintel.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
public class FileScanService {

    private static final List<byte[]> MALICIOUS_SIGNATURES = List.of(
        new byte[]{0x4D, 0x5A},                                         // EXE
        new byte[]{0x50, 0x4B, 0x03, 0x04},                             // ZIP
        new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE} // Java class
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf");

    private static final List<String> DANGEROUS_EXTENSIONS = List.of(
        ".exe", ".bat", ".sh", ".js", ".php",
        ".py", ".cmd", ".vbs", ".ps1", ".jar"
    );

    public ScanResult scan(@NonNull MultipartFile file) throws IOException {
        // getOriginalFilename() is annotated @Nullable in Spring — use a safe fallback
        String filename = Objects.requireNonNullElse(
            file.getOriginalFilename(), ""
        ).toLowerCase();

        for (String dangerous : DANGEROUS_EXTENSIONS) {
            if (filename.endsWith(dangerous)) {
                return new ScanResult(false, "Dangerous file type detected: " + dangerous);
            }
        }

        boolean allowedExt = ALLOWED_EXTENSIONS.stream().anyMatch(filename::endsWith);
        if (!allowedExt) {
            return new ScanResult(false, "Only PDF files are allowed. Got: " + filename);
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return new ScanResult(false, "File too large. Max 5 MB allowed.");
        }

        byte[] bytes = file.getBytes();
        if (bytes.length == 0) {
            return new ScanResult(false, "File is empty.");
        }

        if (bytes.length >= 4) {
            for (byte[] sig : MALICIOUS_SIGNATURES) {
                if (startsWith(bytes, sig)) {
                    return new ScanResult(false, "Malicious file signature detected.");
                }
            }
        }

        return new ScanResult(true, "File is clean");
    }

    private boolean startsWith(byte[] fileBytes, byte[] signature) {
        if (fileBytes.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if (fileBytes[i] != signature[i]) return false;
        }
        return true;
    }

    public record ScanResult(boolean clean, String message) {}
}
