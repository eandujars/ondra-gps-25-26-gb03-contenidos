package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta agregar un item duplicado al carrito.
 */
public class ItemYaEnCarritoException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public ItemYaEnCarritoException(String mensaje) {
        super(mensaje);
    }
}