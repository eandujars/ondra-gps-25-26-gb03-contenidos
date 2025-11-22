package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que representa el carrito de compra de un usuario.
 *
 * <p>Contiene los items del carrito y el cálculo del precio total.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDTO {

    /**
     * Identificador único del carrito.
     */
    private Long idCarrito;

    /**
     * Identificador del usuario propietario del carrito.
     */
    private Long idUsuario;

    /**
     * Lista de items en el carrito.
     */
    private List<CarritoItemDTO> items;

    /**
     * Cantidad total de items en el carrito.
     */
    private int cantidadItems;

    /**
     * Precio total del carrito en euros.
     */
    private BigDecimal precioTotal;

    /**
     * Fecha y hora de creación del carrito.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de última actualización del carrito.
     */
    private LocalDateTime fechaActualizacion;
}