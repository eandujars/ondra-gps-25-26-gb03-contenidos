package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un favorito.
 */
public class FavoritoNotFoundException extends RuntimeException {

    public FavoritoNotFoundException(String mensaje) {
        super(mensaje);
    }
}