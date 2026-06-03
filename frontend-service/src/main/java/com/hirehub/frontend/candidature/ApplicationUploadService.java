package com.hirehub.frontend.candidature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ApplicationUploadService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationUploadService.class);

    private final Path uploadRoot;

    public ApplicationUploadService(@Value("${hirehub.upload-dir:./data/uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
        log.info("[UPLOAD] Dossier fichiers candidatures: {}", uploadRoot);
    }

    public String storeCv(UUID candidatId, String offreId, MultipartFile cv) throws IOException {
        if (cv == null || cv.isEmpty()) {
            throw new IllegalArgumentException("Le CV est obligatoire.");
        }
        String original = cv.getOriginalFilename();
        String ext = ".pdf";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve(offreId);
        Files.createDirectories(dir);
        String fileName = "cv" + ext;
        Path target = dir.resolve(fileName);
        cv.transferTo(target);
        return candidatId + "/" + offreId + "/" + fileName;
    }

    public String storeDefaultCv(UUID candidatId, MultipartFile cv) throws IOException {
        if (cv == null || cv.isEmpty()) {
            throw new IllegalArgumentException("Le CV est obligatoire.");
        }
        String original = cv.getOriginalFilename();
        String ext = ".pdf";
        if (StringUtils.hasText(original) && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve("profile");
        Files.createDirectories(dir);
        String fileName = "cv-default" + ext;
        Path target = dir.resolve(fileName);
        cv.transferTo(target);
        return candidatId + "/profile/" + fileName;
    }

    public boolean hasDefaultCv(UUID candidatId) {
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve("profile");
        if (!Files.isDirectory(dir)) {
            return false;
        }
        try (var stream = Files.list(dir)) {
            return stream.anyMatch(p -> {
                String name = p.getFileName().toString().toLowerCase();
                return name.startsWith("cv") && name.endsWith(".pdf");
            });
        } catch (IOException e) {
            return false;
        }
    }

    public java.util.Optional<Path> resolveStoredFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return java.util.Optional.empty();
        }
        Path file = uploadRoot.resolve(relativePath).normalize();
        if (!file.startsWith(uploadRoot) || !Files.isRegularFile(file)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(file);
    }

    public String storeLettre(UUID candidatId, String offreId, String lettreText) throws IOException {
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve(offreId);
        Files.createDirectories(dir);
        Path target = dir.resolve("lettre.txt");
        String content = StringUtils.hasText(lettreText) ? lettreText : "";
        Files.writeString(target, content, StandardCharsets.UTF_8);
        return candidatId + "/" + offreId + "/lettre.txt";
    }

    public java.util.Optional<String> readLettreText(String relativePath) {
        return resolveStoredFile(relativePath).flatMap(path -> {
            try {
                String text = Files.readString(path, StandardCharsets.UTF_8);
                return StringUtils.hasText(text) ? java.util.Optional.of(text.trim()) : java.util.Optional.empty();
            } catch (IOException e) {
                log.warn("Lecture lettre {} : {}", relativePath, e.getMessage());
                return java.util.Optional.empty();
            }
        });
    }

    public java.util.Optional<Path> resolveDownloadPath(UUID candidatId, String offreId, String fileType) throws IOException {
        Path dir = uploadRoot.resolve(candidatId.toString()).resolve(offreId);
        if (!Files.isDirectory(dir)) {
            return java.util.Optional.empty();
        }
        if ("lettre".equalsIgnoreCase(fileType)) {
            Path lettre = dir.resolve("lettre.txt");
            return Files.exists(lettre) ? java.util.Optional.of(lettre) : java.util.Optional.empty();
        }
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("cv") && (name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx"));
                    })
                    .findFirst();
        }
    }
}
