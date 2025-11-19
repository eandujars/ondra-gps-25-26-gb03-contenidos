package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el endpoint de reproducción.
 * Compatible con el formato esperado por el frontend:
 * { id: string, totalPlays: number }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReproduccionResponseDTO {

    /**
     * ID de la canción como String (para compatibilidad con frontend).
     */
    private String id;

    /**
     * Total de reproducciones después de registrar la reproducción.
     */
    private Long totalPlays;
}