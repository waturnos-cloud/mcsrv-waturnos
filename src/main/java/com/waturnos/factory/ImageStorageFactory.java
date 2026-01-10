package com.waturnos.factory;

import com.waturnos.service.CloudinaryService;
import com.waturnos.service.ImageStorageService;
import com.waturnos.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory para obtener la implementación correcta de ImageStorageService
 * según la configuración definida en storage.images.provider
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageStorageFactory {

    private final CloudinaryService cloudinaryService;
    private final LocalFileStorageService localFileStorageService;

    @Value("${storage.images.provider:CLOUDINARY}")
    private String storageProvider;

    /**
     * Retorna la implementación de ImageStorageService según la configuración
     * @return Implementación configurada de ImageStorageService
     */
    public ImageStorageService getImageStorageService() {
        switch (storageProvider.toUpperCase()) {
            case "CLOUDINARY":
                log.debug("Using Cloudinary as image storage provider");
                return cloudinaryService;
            
            case "LOCALSTORAGE":
                log.debug("Using Local File Storage as image storage provider");
                return localFileStorageService;
            
            case "S3":
                // TODO: Implementar S3Service
                throw new UnsupportedOperationException("S3 storage not implemented yet");
            
            case "GOOGLECLOUDSTORAGE":
                // TODO: Implementar GoogleCloudStorageService
                throw new UnsupportedOperationException("Google Cloud Storage not implemented yet");
            
            default:
                log.warn("Unknown storage provider: {}. Falling back to Cloudinary", storageProvider);
                return cloudinaryService;
        }
    }
}
