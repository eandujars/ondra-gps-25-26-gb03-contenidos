package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de subida de archivos de audio.
 *
 * <p>Contiene la información del archivo de audio subido a Cloudinary
 * incluyendo URL, duración y formato.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioResponseDTO {

    /**
     * URL pública del archivo de audio en Cloudinary.
     */
    private String url;

    /**
     * Mensaje descriptivo de la operación.
     */
    private String mensaje;

    /**
     * Duración del audio en segundos.
     */
    private Integer duracion;

    /**
     * Formato del archivo de audio.
     */
    private String formato;
}