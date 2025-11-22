package com.ondra.contenidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear un nuevo comentario en una canción o álbum.
 *
 * <p>Requiere especificar el tipo de contenido y su identificador correspondiente.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearComentarioDTO {

    /**
     * Tipo de contenido a comentar.
     * Valores válidos: CANCION, ALBUM
     */
    @NotNull(message = "El tipo de contenido es obligatorio")
    private String tipoContenido;

    /**
     * Identificador de la canción.
     * Requerido cuando tipoContenido es CANCION.
     */
    private Long idCancion;

    /**
     * Identificador del álbum.
     * Requerido cuando tipoContenido es ALBUM.
     */
    private Long idAlbum;

    /**
     * Texto del comentario.
     */
    @NotBlank(message = "El contenido del comentario es obligatorio")
    @Size(max = 1000, message = "El comentario no puede exceder los 1000 caracteres")
    private String contenido;
}