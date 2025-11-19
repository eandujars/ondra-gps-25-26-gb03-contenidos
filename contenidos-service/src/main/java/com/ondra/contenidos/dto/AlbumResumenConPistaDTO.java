package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un álbum que contiene una canción específica.
 * Incluye el número de pista de la canción en ese álbum.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResumenConPistaDTO {

    /**
     * ID del álbum.
     */
    private Long idAlbum;

    /**
     * Título del álbum.
     */
    private String tituloAlbum;

    /**
     * URL de la portada del álbum.
     */
    private String urlPortada;

    /**
     * Número de pista de la canción en este álbum.
     */
    private Integer numeroPista;
}