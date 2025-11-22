package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta crear un favorito duplicado.
 */
public class FavoritoYaExisteException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public FavoritoYaExisteException(String mensaje) {
        super(mensaje);
    }
}