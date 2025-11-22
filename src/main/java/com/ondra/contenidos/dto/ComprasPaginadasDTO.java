package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas paginadas de compras.
 *
 * <p>Contiene la lista de compras de la página actual junto con
 * metadatos de paginación para navegación.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprasPaginadasDTO {

    /**
     * Lista de compras de la página actual.
     */
    private List<CompraDTO> compras;

    /**
     * Número de página actual (1-indexed).
     */
    private int paginaActual;

    /**
     * Número total de páginas disponibles.
     */
    private int totalPaginas;

    /**
     * Número total de compras que cumplen los filtros.
     */
    private long totalElementos;

    /**
     * Número de elementos por página.
     */
    private int elementosPorPagina;
}