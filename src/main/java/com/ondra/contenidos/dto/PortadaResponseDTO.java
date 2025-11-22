package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de subida de imágenes de portada.
 *
 * <p>Contiene la información de la imagen de portada subida a Cloudinary
 * incluyendo URL y dimensiones tras la transformación.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortadaResponseDTO {

    /**
     * URL pública de la imagen de portada en Cloudinary.
     */
    private String url;

    /**
     * Mensaje descriptivo de la operación.
     */
    private String mensaje;

    /**
     * Dimensiones de la imagen después de la transformación.
     */
    private String dimensiones;
}