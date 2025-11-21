package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta finalizar compra con un carrito vacío.
 */
public class CarritoVacioException extends RuntimeException {

    public CarritoVacioException(String mensaje) {
        super(mensaje);
    }
}