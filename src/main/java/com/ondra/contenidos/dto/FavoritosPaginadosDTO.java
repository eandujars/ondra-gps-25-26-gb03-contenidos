package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas paginadas de favoritos.
 *
 * <p>Contiene la lista de favoritos de la página actual junto con
 * metadatos de paginación para navegación.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritosPaginadosDTO {

    /**
     * Lista de favoritos de la página actual.
     */
    private List<FavoritoDTO> favoritos;

    /**
     * Número de página actual (1-indexed).
     */
    private int paginaActual;

    /**
     * Número total de páginas disponibles.
     */
    private int totalPaginas;

    /**
     * Número total de favoritos que cumplen los filtros.
     */
    private long totalElementos;

    /**
     * Número de elementos por página.
     */
    private int elementosPorPagina;
}