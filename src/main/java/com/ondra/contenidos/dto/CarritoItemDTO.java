package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa un item individual dentro del carrito de compra.
 *
 * <p>Puede ser una canción o un álbum con su información asociada.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItemDTO {

    /**
     * Identificador único del item en el carrito.
     */
    private Long idCarritoItem;

    /**
     * Tipo de producto.
     * Valores válidos: CANCION, ÁLBUM
     */
    private String tipoProducto;

    /**
     * Identificador de la canción.
     * Presente cuando tipoProducto es CANCION.
     */
    private Long idCancion;

    /**
     * Identificador del álbum.
     * Presente cuando tipoProducto es ÁLBUM.
     */
    private Long idAlbum;

    /**
     * Precio del item en euros.
     */
    private BigDecimal precio;

    /**
     * URL de la portada del contenido en Cloudinary.
     */
    private String urlPortada;

    /**
     * Nombre artístico del autor del contenido.
     */
    private String nombreArtistico;

    /**
     * Título del contenido.
     */
    private String titulo;

    /**
     * Fecha y hora en que se agregó el item al carrito.
     */
    private LocalDateTime fechaAgregado;

    /**
     * Slug del artista para URLs.
     */
    private String slugArtista;
}