# Migración a Cloudinary - Guía Completa

## 📋 Paso 1: Crear cuenta en Cloudinary

1. Ve a https://cloudinary.com/users/register_free
2. Regístrate con tu cuenta de waturnos@gmail.com
3. Completa el formulario:
   - **Email**: waturnos@gmail.com
   - **Nombre del Cloud**: `waturnos` (o el que prefieras)
   - **Tipo de cuenta**: Free (hasta 25GB de almacenamiento y 25GB de transferencia mensuales)

## 🔑 Paso 2: Obtener credenciales

1. Una vez creada la cuenta, ve al Dashboard: https://console.cloudinary.com/
2. En la sección **Product Environment Credentials** encontrarás:
   - **Cloud Name**: waturnos (o el nombre que elegiste)
   - **API Key**: (algo como `123456789012345`)
   - **API Secret**: (algo como `abcdefghijklmnopqrstuvwxyz123`)
3. Guarda estas credenciales, las necesitarás para configurar tu aplicación

## 🛠️ Paso 3: Configurar el proyecto

### 3.1 Agregar dependencia de Cloudinary al pom.xml

Agrega esta dependencia en tu archivo `pom.xml`:

```xml
<!-- Cloudinary SDK -->
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.39.0</version>
</dependency>
```

### 3.2 Configurar variables de entorno

Agrega estas variables de entorno (NO las pongas en application.yml):

```bash
export CLOUDINARY_CLOUD_NAME=waturnos
export CLOUDINARY_API_KEY=tu_api_key_aqui
export CLOUDINARY_API_SECRET=tu_api_secret_aqui
```

Si usas Eclipse, configura estas variables en:
- **Run Configurations** → **Environment** tab → **Add**

### 3.3 Actualizar application.yml

Agrega esta configuración al archivo `src/main/resources/application.yml`:

```yaml
# Cloudinary configuration
cloudinary:
  cloud-name: ${CLOUDINARY_CLOUD_NAME}
  api-key: ${CLOUDINARY_API_KEY}
  api-secret: ${CLOUDINARY_API_SECRET}
  secure: true
```

## 💻 Paso 4: Implementar el servicio

### 4.1 Crear clase de configuración

Crea el archivo `src/main/java/com/waturnos/config/CloudinaryConfig.java`:

```java
package com.waturnos.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }
}
```

### 4.2 Crear servicio de Cloudinary

Crea el archivo `src/main/java/com/waturnos/service/CloudinaryService.java`:

```java
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
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Sube una imagen a Cloudinary
     * @param file Archivo a subir
     * @param imageType Tipo de imagen (para organizar en carpetas)
     * @return URL pública de la imagen
     */
    public String uploadImage(MultipartFile file, ImageType imageType) throws IOException {
        // Configurar opciones de subida
        Map uploadOptions = ObjectUtils.asMap(
            "folder", "waturnos/" + imageType.getSubdir(), // Organizar por carpetas
            "resource_type", "image",
            "transformation", ObjectUtils.asMap(
                "quality", "auto",  // Optimización automática de calidad
                "fetch_format", "auto"  // Formato automático (WebP si es soportado)
            )
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
     * @param publicId ID público de la imagen en Cloudinary
     */
    public void deleteImage(String publicId) throws IOException {
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.info("Imagen eliminada de Cloudinary: {} - Resultado: {}", publicId, result.get("result"));
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
```

### 4.3 Actualizar el controlador

Modifica `src/main/java/com/waturnos/controller/UploadController.java` para usar Cloudinary:

```java
package com.waturnos.controller;

import com.waturnos.dto.response.UploadResponse;
import com.waturnos.enums.ImageType;
import com.waturnos.service.CloudinaryService;
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

    private final CloudinaryService cloudinaryService;

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
            // Subir a Cloudinary
            String url = cloudinaryService.uploadImage(file, imageType);

            log.info("File uploaded successfully to Cloudinary: {}", url);

            return ResponseEntity.ok(UploadResponse.builder().url(url).build());

        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }

    @DeleteMapping("/image")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String imageUrl) {
        try {
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            if (publicId == null) {
                return ResponseEntity.badRequest().body("Invalid Cloudinary URL");
            }

            cloudinaryService.deleteImage(publicId);
            return ResponseEntity.ok().body("Image deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary", e);
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
```

## 🔄 Paso 5: Migrar imágenes existentes (Opcional)

Si ya tienes imágenes en el servidor local y quieres migrarlas:

### 5.1 Crear script de migración

Crea `src/main/java/com/waturnos/util/ImageMigrationScript.java`:

```java
package com.waturnos.util;

import com.waturnos.service.CloudinaryService;
import com.waturnos.enums.ImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Component
@Profile("migrate-images") // Solo se ejecuta si activas este perfil
@RequiredArgsConstructor
public class ImageMigrationScript implements CommandLineRunner {

    private final CloudinaryService cloudinaryService;

    @Value("${app.upload.base-dir:/var/waturnos/images/}")
    private String baseDir;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando migración de imágenes a Cloudinary...");

        for (ImageType imageType : ImageType.values()) {
            migrateImageType(imageType);
        }

        log.info("Migración completada!");
    }

    private void migrateImageType(ImageType imageType) throws Exception {
        Path typePath = Paths.get(baseDir, imageType.getSubdir());
        
        if (!Files.exists(typePath)) {
            log.info("No hay imágenes para migrar en: {}", typePath);
            return;
        }

        log.info("Migrando imágenes de tipo: {} desde {}", imageType, typePath);

        try (Stream<Path> paths = Files.walk(typePath)) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                     try {
                         File file = path.toFile();
                         FileInputStream input = new FileInputStream(file);
                         MockMultipartFile multipartFile = new MockMultipartFile(
                             "file",
                             file.getName(),
                             Files.probeContentType(path),
                             input
                         );

                         String url = cloudinaryService.uploadImage(multipartFile, imageType);
                         log.info("Migrado: {} -> {}", file.getName(), url);

                     } catch (Exception e) {
                         log.error("Error migrando archivo: {}", path, e);
                     }
                 });
        }
    }
}
```

### 5.2 Ejecutar la migración

Para ejecutar la migración de imágenes existentes, ejecuta:

```bash
export SPRING_PROFILES_ACTIVE=migrate-images
export CLOUDINARY_CLOUD_NAME=tu_cloud_name
export CLOUDINARY_API_KEY=tu_api_key
export CLOUDINARY_API_SECRET=tu_api_secret
./start-server.sh
```

**IMPORTANTE**: Este script subirá TODAS las imágenes del servidor local a Cloudinary. Asegúrate de:
1. Tener suficiente espacio en tu plan de Cloudinary
2. Hacer un backup de tus imágenes antes de empezar
3. Actualizar las URLs en tu base de datos después de la migración

## 📦 Paso 6: Actualizar base de datos

Si tienes URLs de imágenes guardadas en la base de datos como `/images/logos/123456-logo.png`, necesitarás:

1. Crear un script SQL para actualizar las URLs antiguas
2. O crear un endpoint que devuelva las imágenes antiguas sirviendo desde Cloudinary

### Opción A: Mantener compatibilidad con URLs antiguas

Crea un nuevo endpoint en UploadController:

```java
@GetMapping("/images/{type}/{filename}")
public ResponseEntity<Void> redirectOldImage(
        @PathVariable String type,
        @PathVariable String filename) {
    // Redirigir a Cloudinary manteniendo la estructura de carpetas
    String cloudinaryUrl = String.format(
        "https://res.cloudinary.com/waturnos/image/upload/waturnos/%s/%s",
        type, filename
    );
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
            .header("Location", cloudinaryUrl)
            .build();
}
```

## ✅ Paso 7: Probar la integración

1. Reinicia tu servidor con las variables de entorno configuradas
2. Usa Postman para probar el endpoint de upload:

```bash
POST http://localhost:8085/msvc-waturnos/v1.0/upload/image
Content-Type: multipart/form-data

file: [seleccionar archivo]
type: LOGO
```

3. Deberías recibir una respuesta con una URL de Cloudinary:

```json
{
  "url": "https://res.cloudinary.com/waturnos/image/upload/v1234567890/waturnos/logos/abc123.jpg"
}
```

## 🎯 Ventajas de Cloudinary

✅ **CDN Global**: Las imágenes se sirven desde el servidor más cercano al usuario
✅ **Optimización automática**: Conversión a WebP, compresión inteligente
✅ **Transformaciones on-the-fly**: Redimensionar, recortar, aplicar efectos
✅ **Backup automático**: No dependes del almacenamiento local
✅ **Sin gestión de archivos**: No necesitas limpiar el disco del servidor
✅ **URLs permanentes**: Las imágenes no se pierden si reinicias el servidor

## 🔒 Seguridad

- **NUNCA** expongas tu API Secret en el código o repositorio
- Usa variables de entorno para las credenciales
- Considera implementar firma de URLs para uploads seguros
- Limita los tipos de archivos y tamaños permitidos

## 📊 Monitoreo

Puedes ver estadísticas de uso en: https://console.cloudinary.com/console/usage

- Almacenamiento usado
- Ancho de banda consumido
- Número de transformaciones
- Llamadas a la API

---

¿Preguntas o problemas? Revisa la documentación oficial: https://cloudinary.com/documentation
