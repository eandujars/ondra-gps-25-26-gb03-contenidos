package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un carrito.
 */
public class CarritoNotFoundException extends RuntimeException {

    public CarritoNotFoundException(String mensaje) {
        super(mensaje);
    }
}