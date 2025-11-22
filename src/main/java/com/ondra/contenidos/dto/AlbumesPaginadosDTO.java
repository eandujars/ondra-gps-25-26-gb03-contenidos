package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas paginadas de álbumes.
 *
 * <p>Contiene la lista de álbumes de la página actual junto con
 * metadatos de paginación para navegación.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumesPaginadosDTO {

    /**
     * Lista de álbumes de la página actual.
     */
    private List<AlbumDTO> albumes;

    /**
     * Número de página actual (1-indexed).
     */
    private Integer paginaActual;

    /**
     * Número total de páginas disponibles.
     */
    private Integer totalPaginas;

    /**
     * Número total de álbumes que cumplen los filtros.
     */
    private Long totalElementos;

    /**
     * Número de elementos por página.
     */
    private Integer elementosPorPagina;
}