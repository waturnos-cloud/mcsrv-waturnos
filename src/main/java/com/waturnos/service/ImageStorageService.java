package com.waturnos.service;

import com.waturnos.enums.ImageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interfaz para servicios de almacenamiento de imágenes.
 * Permite cambiar de proveedor (Cloudinary, S3, Azure Blob, etc.) sin afectar el código cliente.
 */
public interface ImageStorageService {
    
    /**
     * Sube una imagen al servicio de almacenamiento
     * @param file Archivo a subir
     * @param imageType Tipo de imagen (para organizar en carpetas)
     * @return URL pública de la imagen
     */
    String uploadImage(MultipartFile file, ImageType imageType) throws IOException;
    
    /**
     * Elimina una imagen del servicio de almacenamiento
     * @param imageUrl URL completa de la imagen
     */
    void deleteImage(String imageUrl) throws IOException;
    
    /**
     * Verifica si una URL pertenece a este servicio de almacenamiento
     * @param imageUrl URL a verificar
     * @return true si la URL pertenece a este servicio
     */
    boolean isValidUrl(String imageUrl);
}
