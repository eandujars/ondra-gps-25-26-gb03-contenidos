package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con información detallada de una canción.
 *
 * <p>Incluye metadatos completos de la canción y la lista de álbumes
 * que la contienen con su número de pista correspondiente.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancionDetalleDTO {

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
     * Número total de reproducciones.
     */
    private Long reproducciones;

    /**
     * Valoración media de la canción.
     */
    private Double valoracionMedia;

    /**
     * Número total de comentarios.
     */
    private Long totalComentarios;

    /**
     * Fecha y hora de publicación.
     */
    private LocalDateTime fechaPublicacion;

    /**
     * Descripción de la canción.
     */
    private String descripcion;

    /**
     * Lista de álbumes que contienen esta canción.
     * Incluye el número de pista en cada álbum.
     */
    private List<AlbumResumenConPistaDTO> albumes;
}