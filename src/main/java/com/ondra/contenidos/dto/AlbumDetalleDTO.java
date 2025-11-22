package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con información detallada de un álbum.
 *
 * <p>Incluye la lista completa de canciones ordenadas por número de pista
 * y estadísticas agregadas del álbum.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetalleDTO {

    /**
     * Identificador único del álbum.
     */
    private Long idAlbum;

    /**
     * Título del álbum.
     */
    private String tituloAlbum;

    /**
     * Identificador del artista propietario.
     */
    private Long idArtista;

    /**
     * Nombre del género musical predominante.
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
     * Número total de canciones en el álbum.
     */
    private Integer totalCanciones;

    /**
     * Duración total del álbum en segundos.
     */
    private Integer duracionTotalSegundos;

    /**
     * Total de reproducciones del álbum.
     */
    private Long totalPlayCount;

    /**
     * Valoración media del álbum.
     */
    private Double valoracionMedia;

    /**
     * Número total de comentarios del álbum.
     */
    private Long totalComentarios;

    /**
     * Fecha y hora de publicación del álbum.
     */
    private LocalDateTime fechaPublicacion;

    /**
     * Descripción del álbum.
     */
    private String descripcion;

    /**
     * Lista de canciones del álbum ordenadas por número de pista.
     */
    private List<CancionAlbumDTO> trackList;
}