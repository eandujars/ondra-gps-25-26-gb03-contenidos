package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un carrito.
 *
 * <p>Se utiliza en operaciones sobre carritos que no existen
 * o que no están asociados al usuario actual.</p>
 */
public class CarritoNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje mensaje descriptivo del error
     */
    public CarritoNotFoundException(String mensaje) {
        super(mensaje);
    }
}