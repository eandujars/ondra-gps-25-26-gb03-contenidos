package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con estadísticas de reproducciones de un artista.
 * Usado por el microservicio Usuarios para mostrar estadísticas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasArtistaDTO {

    /**
     * ID del artista.
     */
    private Long idArtista;

    /**
     * Total de reproducciones de todas las canciones del artista.
     */
    private Long totalReproducciones;
}