package com.ondra.contenidos.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Cloudinary para el microservicio de Contenidos.
 *
 * <p>Esta clase crea y configura el bean de Cloudinary necesario
 * para realizar operaciones de subida, transformación y eliminación
 * de archivos multimedia (audio e imágenes).</p>
 *
 * <p>Las credenciales se inyectan desde application.properties:</p>
 * <ul>
 *   <li>cloudinary.cloud-name</li>
 *   <li>cloudinary.api-key</li>
 *   <li>cloudinary.api-secret</li>
 * </ul>
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
     * Crea y configura el bean de Cloudinary.
     *
     * <p>El bean se configura con las credenciales proporcionadas
     * en application.properties y se inyecta automáticamente en
     * {@link com.ondra.contenidos.services.CloudinaryService}.</p>
     *
     * @return Instancia configurada de Cloudinary
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