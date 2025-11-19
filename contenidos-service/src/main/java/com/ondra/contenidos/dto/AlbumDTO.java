package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO principal para transferencia de información de álbumes.
 * Compatible con el modelo Album del frontend Angular.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {

    /**
     * ID único del álbum.
     */
    private Long idAlbum;

    /**
     * Título del álbum.
     */
    private String tituloAlbum;

    /**
     * ID del artista propietario.
     * El frontend obtiene los datos completos del artista del microservicio Usuarios.
     */
    private Long idArtista;

    /**
     * Nombre del género musical predominante (e.g., "Rock", "Pop").
     */
    private String genero;

    /**
     * Precio del álbum completo en euros.
     */
    private Double precioAlbum;

    /**
     * URL de la portada del álbum en Cloudinary.
     */
    private String urlPortada;

    /**
     * Valoración media del álbum (calculada desde comentarios).
     * Será implementado cuando se cree el módulo de comentarios.
     */
    private Double valoracionMedia;

    /**
     * Número total de comentarios del álbum.
     * Será implementado cuando se cree el módulo de comentarios.
     */
    private Long totalComentarios;

    /**
     * Número total de canciones en el álbum.
     */
    private Integer totalCanciones;

    /**
     * Duración total del álbum en segundos.
     */
    private Integer duracionTotalSegundos;

    /**
     * Total de reproducciones del álbum (suma de reproducciones de todas sus canciones).
     */
    private Long totalPlayCount;

    /**
     * Fecha y hora de publicación del álbum.
     */
    private LocalDateTime fechaPublicacion;

    /**
     * Descripción del álbum.
     */
    private String descripcion;
}