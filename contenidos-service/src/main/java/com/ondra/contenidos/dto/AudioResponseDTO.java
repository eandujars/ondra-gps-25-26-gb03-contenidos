package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de subida de archivos de audio.
 *
 * <p>Contiene la información del audio subido a Cloudinary.</p>
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
     * Duración del audio en segundos (opcional).
     * Puede ser null si no se calcula en el momento de la subida.
     */
    private Integer duracion;

    /**
     * Formato del archivo de audio (ej: "mp3", "wav", "flac").
     */
    private String formato;
}