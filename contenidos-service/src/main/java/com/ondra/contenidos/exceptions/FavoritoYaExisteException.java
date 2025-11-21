package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta agregar un favorito que ya existe.
 */
public class FavoritoYaExisteException extends RuntimeException {

    public FavoritoYaExisteException(String mensaje) {
        super(mensaje);
    }
}