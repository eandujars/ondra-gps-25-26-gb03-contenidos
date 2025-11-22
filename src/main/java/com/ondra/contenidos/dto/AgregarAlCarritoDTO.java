package com.ondra.contenidos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para agregar un producto al carrito de compra.
 *
 * <p>Permite añadir canciones o álbumes al carrito del usuario.
 * Debe especificarse el tipo de producto y el identificador correspondiente.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgregarAlCarritoDTO {

    /**
     * Tipo de producto a agregar.
     * Valores válidos: CANCION, ALBUM
     */
    @NotNull(message = "El tipo de producto es obligatorio")
    private String tipoProducto;

    /**
     * Identificador de la canción.
     * Requerido cuando tipoProducto es CANCION.
     */
    private Long idCancion;

    /**
     * Identificador del álbum.
     * Requerido cuando tipoProducto es ALBUM.
     */
    private Long idAlbum;
}