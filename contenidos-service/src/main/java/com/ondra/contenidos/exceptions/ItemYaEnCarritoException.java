package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta agregar un item que ya está en el carrito.
 */
public class ItemYaEnCarritoException extends RuntimeException {

    public ItemYaEnCarritoException(String mensaje) {
        super(mensaje);
    }
}