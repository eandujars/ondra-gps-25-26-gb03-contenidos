package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un favorito en el sistema.
 */
public class FavoritoNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public FavoritoNotFoundException(String mensaje) {
        super(mensaje);
    }
}