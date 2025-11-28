package com.waturnos.controller;

import com.waturnos.dto.response.UploadResponse;
import com.waturnos.enums.ImageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${app.upload.base-dir:/var/waturnos/images/}")
    private String baseDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        // Validate file is not empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        // Validate image type
        ImageType imageType;
        try {
            imageType = ImageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid image type. Allowed: LOGO, AVATAR, BANNER, SERVICE_IMAGE");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body("File size exceeds maximum allowed (5MB)");
        }

        // Get and validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid filename");
        }

        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ResponseEntity.badRequest().body("Invalid file extension. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        try {
            // Sanitize filename and create unique name
            String sanitizedName = sanitizeFilename(originalFilename);
            String filename = System.currentTimeMillis() + "-" + sanitizedName;

            // Create directory structure
            Path uploadPath = Paths.get(baseDir, imageType.getSubdir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Build response URL
            String url = "/images/" + imageType.getSubdir() + "/" + filename;

            log.info("File uploaded successfully: {}", url);

            return ResponseEntity.ok(UploadResponse.builder().url(url).build());

        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String sanitizeFilename(String filename) {
        // Remove path separators and other potentially dangerous characters
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Ensure the extension is preserved
        String extension = getFileExtension(filename);
        String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
        
        // Limit name length (excluding extension)
        if (nameWithoutExt.length() > 50) {
            nameWithoutExt = nameWithoutExt.substring(0, 50);
        }
        
        return nameWithoutExt + "." + extension;
    }
}
