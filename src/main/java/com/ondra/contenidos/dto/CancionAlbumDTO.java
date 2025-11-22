package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una canción dentro de un álbum.
 *
 * <p>Incluye información de la canción y su posición en el álbum.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancionAlbumDTO {

    /**
     * Identificador de la canción.
     */
    private Long idCancion;

    /**
     * Título de la canción.
     */
    private String tituloCancion;

    /**
     * Duración de la canción en segundos.
     */
    private Integer duracionSegundos;

    /**
     * Número de pista en el álbum (1-indexed).
     */
    private Integer trackNumber;

    /**
     * URL de la portada de la canción en Cloudinary.
     */
    private String urlPortada;

    /**
     * URL del archivo de audio en Cloudinary.
     */
    private String urlAudio;

    /**
     * Precio individual de la canción en euros.
     */
    private Double precioCancion;

    /**
     * Número total de reproducciones de la canción.
     */
    private Long reproducciones;
}