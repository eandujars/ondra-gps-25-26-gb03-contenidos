package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa un favorito de un usuario.
 *
 * <p>Puede contener una canción o un álbum marcado como favorito
 * con información del artista asociado.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoDTO {

    /**
     * Identificador único del favorito.
     */
    private Long idFavorito;

    /**
     * Identificador del usuario propietario del favorito.
     */
    private Long idUsuario;

    /**
     * Tipo de contenido marcado como favorito.
     * Valores válidos: CANCION, ALBUM
     */
    private String tipoContenido;

    /**
     * Información de la canción favorita.
     * Presente cuando tipoContenido es CANCION.
     */
    private CancionDTO cancion;

    /**
     * Información del álbum favorito.
     * Presente cuando tipoContenido es ALBUM.
     */
    private AlbumDTO album;

    /**
     * Fecha y hora en que se agregó a favoritos.
     */
    private LocalDateTime fechaAgregado;

    /**
     * Nombre del artista del contenido favorito.
     */
    private String nombreArtista;
}