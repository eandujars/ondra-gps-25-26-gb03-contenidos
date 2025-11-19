package com.ondra.contenidos.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Cloudinary para gestión de archivos multimedia.
 *
 * <p>Define el bean de Cloudinary utilizado para operaciones de subida,
 * transformación y eliminación de archivos de audio e imágenes.</p>
 *
 * <p>Credenciales requeridas en application.properties:
 * cloudinary.cloud-name, cloudinary.api-key, cloudinary.api-secret</p>
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    /**
     * Configura el bean de Cloudinary con credenciales de aplicación.
     *
     * @return instancia configurada de Cloudinary con conexión segura habilitada
     */
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