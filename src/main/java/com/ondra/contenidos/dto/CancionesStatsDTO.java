package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estadísticas generales del catálogo de canciones.
 *
 * <p>Contiene métricas agregadas sobre el total de canciones
 * y reproducciones del sistema.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancionesStatsDTO {

    /**
     * Número total de canciones en el catálogo.
     */
    private Long totalCanciones;

    /**
     * Número total de reproducciones acumuladas de todas las canciones.
     */
    private Long totalReproducciones;
}