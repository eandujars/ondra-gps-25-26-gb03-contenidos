package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con información detallada de una canción.
 * Incluye la lista de álbumes que contienen esta canción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancionDetalleDTO {

    private Long idCancion;
    private String tituloCancion;
    private Long idArtista;
    private String genero;
    private Double precioCancion;
    private Integer duracionSegundos;
    private String urlPortada;
    private String urlAudio;
    private Long reproducciones;
    private Double valoracionMedia;
    private Long totalComentarios;
    private LocalDateTime fechaPublicacion;
    private String descripcion;

    /**
     * Lista de álbumes que contienen esta canción.
     * Incluye el número de pista en cada álbum.
     */
    private List<AlbumResumenConPistaDTO> albumes;
}