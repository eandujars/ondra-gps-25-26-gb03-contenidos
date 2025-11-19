package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con información detallada de un álbum.
 * Incluye la lista completa de canciones (trackList) ordenadas por número de pista.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetalleDTO {

    private Long idAlbum;
    private String tituloAlbum;
    private Long idArtista;
    private String genero;
    private Double precioAlbum;
    private String urlPortada;
    private Integer totalCanciones;
    private Integer duracionTotalSegundos;
    private Long totalPlayCount;
    private Double valoracionMedia;
    private Long totalComentarios;
    private LocalDateTime fechaPublicacion;
    private String descripcion;

    /**
     * Lista completa de canciones del álbum ordenadas por número de pista.
     * Compatible con el trackList del frontend (AlbumTrack[]).
     */
    private List<CancionAlbumDTO> trackList;
}
