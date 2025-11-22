package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas paginadas de valoraciones.
 *
 * <p>Contiene la lista de valoraciones de la página actual junto con
 * metadatos de paginación y la valoración promedio del contenido.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValoracionesPaginadasDTO {

    /**
     * Lista de valoraciones de la página actual.
     */
    private List<ValoracionDTO> valoraciones;

    /**
     * Número de página actual (1-indexed).
     */
    private Integer paginaActual;

    /**
     * Número total de páginas disponibles.
     */
    private Integer totalPaginas;

    /**
     * Número total de valoraciones que cumplen los filtros.
     */
    private Long totalElementos;

    /**
     * Número de elementos por página.
     */
    private Integer elementosPorPagina;

    /**
     * Valoración promedio del contenido.
     */
    private Double valoracionPromedio;
}