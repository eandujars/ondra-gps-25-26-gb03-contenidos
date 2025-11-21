package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un item del carrito.
 */
public class CarritoItemNotFoundException extends RuntimeException {

    public CarritoItemNotFoundException(String mensaje) {
        super(mensaje);
    }
}