package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un álbum que contiene una canción específica.
 *
 * <p>Incluye información básica del álbum y el número de pista
 * de la canción dentro del mismo.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResumenConPistaDTO {

    /**
     * Identificador del álbum.
     */
    private Long idAlbum;

    /**
     * Título del álbum.
     */
    private String tituloAlbum;

    /**
     * URL de la portada del álbum en Cloudinary.
     */
    private String urlPortada;

    /**
     * Número de pista de la canción en este álbum.
     */
    private Integer numeroPista;
}