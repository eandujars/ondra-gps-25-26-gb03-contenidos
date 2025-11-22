package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de información de canciones.
 *
 * <p>Contiene los datos principales de la canción incluyendo metadatos,
 * estadísticas y referencias al artista y álbum propietarios.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancionDTO {

    /**
     * Identificador único de la canción.
     */
    private Long idCancion;

    /**
     * Título de la canción.
     */
    private String tituloCancion;

    /**
     * Identificador del artista propietario.
     */
    private Long idArtista;

    /**
     * Nombre del género musical.
     */
    private String genero;

    /**
     * Precio de la canción en euros.
     */
    private Double precioCancion;

    /**
     * Duración de la canción en segundos.
     */
    private Integer duracionSegundos;

    /**
     * URL de la portada de la canción en Cloudinary.
     */
    private String urlPortada;

    /**
     * URL del archivo de audio en Cloudinary.
     */
    private String urlAudio;

    /**
     * Número total de reproducciones de la canción.
     */
    private Long reproducciones;

    /**
     * Valoración media de la canción.
     */
    private Double valoracionMedia;

    /**
     * Número total de comentarios de la canción.
     */
    private Long totalComentarios;

    /**
     * Fecha y hora de publicación de la canción.
     */
    private LocalDateTime fechaPublicacion;

    /**
     * Descripción de la canción.
     */
    private String descripcion;

    /**
     * Información resumida del álbum al que pertenece.
     * Puede ser null si la canción no está en ningún álbum.
     */
    private AlbumResumenDTO album;
}