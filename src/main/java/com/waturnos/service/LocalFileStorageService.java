package com.waturnos.service;

import com.waturnos.enums.ImageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementación de almacenamiento local de imágenes.
 * Útil para desarrollo o como fallback cuando no se quiere usar servicios en la nube.
 * 
 * Para activar este servicio en vez de Cloudinary:
 * 1. Comentar @Primary en CloudinaryService
 * 2. Agregar @Primary aquí
 * 3. O usar @Qualifier("localFileStorageService") donde lo inyectes
 */
@Slf4j
@Service("localFileStorageService")
public class LocalFileStorageService implements ImageStorageService {

    @Value("${app.upload.base-dir:/var/waturnos/images/}")
    private String baseDir;

    @Value("${app.upload.base-url:http://localhost:8085/images/}")
    private String baseUrl;

    @Override
    public String uploadImage(MultipartFile file, ImageType imageType) throws IOException {
        // Sanitizar y crear nombre único
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String sanitizedName = sanitizeFilename(originalFilename);
        String filename = System.currentTimeMillis() + "-" + sanitizedName;

        // Crear estructura de directorios
        Path uploadPath = Paths.get(baseDir, imageType.getSubdir());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Guardar archivo
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        // Construir URL
        String url = baseUrl + imageType.getSubdir() + "/" + filename;

        log.info("Imagen guardada localmente: {} -> {}", filePath, url);

        return url;
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {
        if (!isValidUrl(imageUrl)) {
            throw new IllegalArgumentException("Invalid local file URL: " + imageUrl);
        }

        // Extraer path relativo de la URL
        String relativePath = imageUrl.replace(baseUrl, "");
        Path filePath = Paths.get(baseDir, relativePath);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Imagen eliminada localmente: {}", filePath);
        } else {
            log.warn("Archivo no encontrado para eliminar: {}", filePath);
        }
    }

    @Override
    public boolean isValidUrl(String imageUrl) {
        return imageUrl != null && imageUrl.startsWith(baseUrl);
    }

    private String sanitizeFilename(String filename) {
        // Remover caracteres peligrosos
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Asegurar que la extensión se preserve
        String extension = getFileExtension(filename);
        String nameWithoutExt = sanitized.substring(0, sanitized.lastIndexOf('.'));
        
        // Limitar longitud del nombre
        if (nameWithoutExt.length() > 50) {
            nameWithoutExt = nameWithoutExt.substring(0, 50);
        }
        
        return nameWithoutExt + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
