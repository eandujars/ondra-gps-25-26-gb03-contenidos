package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para el registro de reproducción de una canción.
 *
 * <p>Contiene el identificador de la canción y el total de reproducciones actualizado.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReproduccionResponseDTO {

    /**
     * Identificador de la canción reproducida.
     */
    private String id;

    /**
     * Total de reproducciones después de registrar la reproducción actual.
     */
    private Long totalPlays;
}