package com.ondra.contenidos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para agregar contenido a la lista de favoritos.
 *
 * <p>Permite añadir canciones o álbumes a los favoritos del usuario.
 * Debe especificarse el tipo de contenido y el identificador correspondiente.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgregarFavoritoDTO {

    /**
     * Tipo de contenido a marcar como favorito.
     * Valores válidos: CANCION, ÁLBUM
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
     * Requerido cuando tipoContenido es ÁLBUM.
     */
    private Long idAlbum;
}