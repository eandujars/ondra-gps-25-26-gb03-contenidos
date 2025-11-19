package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa una canción dentro de un álbum.
 * Compatible con AlbumTrack del frontend Angular.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancionAlbumDTO {

    /**
     * ID de la canción.
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
     * URL de la portada de la canción.
     */
    private String urlPortada;

    /**
     * URL del archivo de audio.
     */
    private String urlAudio;

    /**
     * Precio individual de la canción.
     */
    private Double precioCancion;

    /**
     * Número de reproducciones de la canción.
     */
    private Long reproducciones;
}