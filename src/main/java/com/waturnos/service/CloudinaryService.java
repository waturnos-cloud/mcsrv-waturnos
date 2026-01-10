package com.waturnos.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.waturnos.enums.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService implements ImageStorageService {

    private final Cloudinary cloudinary;

    /**
     * Sube una imagen a Cloudinary
     * @param file Archivo a subir
     * @param imageType Tipo de imagen (para organizar en carpetas)
     * @return URL pública de la imagen
     */
    @Override
    public String uploadImage(MultipartFile file, ImageType imageType) throws IOException {
        // Configurar opciones de subida
        Map uploadOptions = ObjectUtils.asMap(
            "folder", "waturnos/" + imageType.getSubdir(), // Organizar por carpetas
            "resource_type", "image",
            "quality", "auto"  // Optimización automática de calidad
        );

        // Subir archivo
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        
        // Obtener URL segura (HTTPS)
        String url = (String) uploadResult.get("secure_url");
        
        log.info("Imagen subida a Cloudinary: {} - Carpeta: {}", url, imageType.getSubdir());
        
        return url;
    }

    /**
     * Elimina una imagen de Cloudinary
     * @param imageUrl URL completa de la imagen en Cloudinary
     */
    @Override
    public void deleteImage(String imageUrl) throws IOException {
        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            throw new IllegalArgumentException("Invalid Cloudinary URL: " + imageUrl);
        }
        
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.info("Imagen eliminada de Cloudinary: {} - Resultado: {}", publicId, result.get("result"));
    }

    /**
     * Verifica si una URL pertenece a Cloudinary
     * @param imageUrl URL a verificar
     * @return true si es una URL de Cloudinary
     */
    @Override
    public boolean isValidUrl(String imageUrl) {
        return imageUrl != null && imageUrl.contains("cloudinary.com");
    }

    /**
     * Extrae el public_id de una URL de Cloudinary
     * @param imageUrl URL completa de Cloudinary
     * @return public_id o null si no es una URL válida
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // URL formato: https://res.cloudinary.com/{cloud_name}/image/upload/{transformations}/{public_id}.{format}
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;
            
            String pathAfterUpload = parts[1];
            // Saltar transformaciones si existen (ej: v1234567890/ o w_500,h_500/)
            String[] pathParts = pathAfterUpload.split("/");
            
            StringBuilder publicId = new StringBuilder();
            boolean foundFolder = false;
            for (String part : pathParts) {
                // Saltar versiones y transformaciones
                if (part.matches("v\\d+")) continue;
                if (part.contains("w_") || part.contains("h_") || part.contains("c_")) continue;
                
                if (foundFolder || part.equals("waturnos")) {
                    foundFolder = true;
                    if (publicId.length() > 0) publicId.append("/");
                    // Remover extensión del último segmento
                    if (part.contains(".")) {
                        publicId.append(part.substring(0, part.lastIndexOf(".")));
                    } else {
                        publicId.append(part);
                    }
                }
            }
            
            return publicId.toString();
        } catch (Exception e) {
            log.error("Error extrayendo public_id de URL: {}", imageUrl, e);
            return null;
        }
    }
}
