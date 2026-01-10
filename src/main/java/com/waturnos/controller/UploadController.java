package com.waturnos.controller;

import com.waturnos.dto.response.UploadResponse;
import com.waturnos.enums.ImageType;
import com.waturnos.factory.ImageStorageFactory;
import com.waturnos.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final ImageStorageFactory imageStorageFactory;

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
            return ResponseEntity.badRequest().body("Invalid image type. Allowed: LOGO, AVATAR, PROVIDER_IMAGE, SERVICE_IMAGE");
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
            // Obtener el servicio de almacenamiento configurado
            ImageStorageService imageStorageService = imageStorageFactory.getImageStorageService();
            
            // Subir usando el servicio de almacenamiento configurado
            String url = imageStorageService.uploadImage(file, imageType);

            log.info("File uploaded successfully: {}", url);

            return ResponseEntity.ok(UploadResponse.builder().url(url).build());

        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            // Obtener el servicio de almacenamiento configurado
            ImageStorageService imageStorageService = imageStorageFactory.getImageStorageService();
            
            if (!imageStorageService.isValidUrl(imageUrl)) {
                return ResponseEntity.badRequest().body("Invalid image URL for current storage service");
            }

            imageStorageService.deleteImage(imageUrl);
            return ResponseEntity.ok().body("Image deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting image: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
