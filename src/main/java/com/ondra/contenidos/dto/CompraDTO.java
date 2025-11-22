package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa una compra realizada por un usuario.
 *
 * <p>Contiene información de la transacción y detalles del contenido adquirido.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraDTO {

    /**
     * Identificador único de la compra.
     */
    private Long idCompra;

    /**
     * Identificador del usuario que realizó la compra.
     */
    private Long idUsuario;

    /**
     * Tipo de contenido adquirido.
     * Valores válidos: CANCION, ALBUM
     */
    private String tipoContenido;

    /**
     * Información de la canción adquirida.
     * Presente cuando tipoContenido es CANCION.
     */
    private CancionDTO cancion;

    /**
     * Información del álbum adquirido.
     * Presente cuando tipoContenido es ALBUM.
     */
    private AlbumDTO album;

    /**
     * Precio pagado por el contenido en euros.
     */
    private BigDecimal precioPagado;

    /**
     * Fecha y hora de la compra.
     */
    private LocalDateTime fechaCompra;

    /**
     * Método de pago utilizado en la transacción.
     */
    private String metodoPago;

    /**
     * Identificador único de la transacción.
     */
    private String idTransaccion;

    /**
     * Nombre del artista del contenido adquirido.
     */
    private String nombreArtista;
}