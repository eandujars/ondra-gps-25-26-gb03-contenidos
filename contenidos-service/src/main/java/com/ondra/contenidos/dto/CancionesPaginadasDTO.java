package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas paginadas de canciones.
 * Compatible con el formato esperado por el frontend Angular.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancionesPaginadasDTO {

    /**
     * Lista de canciones de la página actual.
     */
    private List<CancionDTO> canciones;

    /**
     * Número de página actual (1-indexed para frontend).
     */
    private Integer paginaActual;

    /**
     * Número total de páginas disponibles.
     */
    private Integer totalPaginas;

    /**
     * Número total de canciones que cumplen los filtros.
     */
    private Long totalElementos;

    /**
     * Número de elementos por página.
     */
    private Integer elementosPorPagina;
}