package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un item del carrito.
 *
 * <p>Se utiliza en operaciones de actualización o eliminación
 * de items que no existen en el carrito.</p>
 */
public class CarritoItemNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje mensaje descriptivo del error
     */
    public CarritoItemNotFoundException(String mensaje) {
        super(mensaje);
    }
}