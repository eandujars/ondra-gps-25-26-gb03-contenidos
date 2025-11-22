package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con estadísticas de reproducciones de un artista.
 *
 * <p>Utilizado para proporcionar métricas de reproducciones
 * al microservicio de Usuarios.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasArtistaDTO {

    /**
     * Identificador del artista.
     */
    private Long idArtista;

    /**
     * Total de reproducciones de todas las canciones del artista.
     */
    private Long totalReproducciones;
}