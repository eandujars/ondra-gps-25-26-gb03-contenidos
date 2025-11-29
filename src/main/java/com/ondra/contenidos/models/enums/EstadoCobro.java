package com.ondra.contenidos.models.enums;

/**
 * Estados posibles de un cobro.
 */
public enum EstadoCobro {
    /**
     * Cobro generado pero aún no pagado al artista.
     */
    PENDIENTE,

    /**
     * Cobro ya pagado al artista.
     */
    PAGADO,

    /**
     * Cobro cancelado (ej: devolución de compra).
     */
    CANCELADO
}