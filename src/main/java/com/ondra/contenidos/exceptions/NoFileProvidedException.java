package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se proporciona un archivo en la petición.
 */
public class NoFileProvidedException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message descripción del error
     */
    public NoFileProvidedException(String message) {
        super(message);
    }
}