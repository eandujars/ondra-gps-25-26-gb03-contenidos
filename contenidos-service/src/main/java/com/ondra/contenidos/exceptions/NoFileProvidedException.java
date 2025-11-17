package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se proporciona ningún archivo en la petición.
 */
public class NoFileProvidedException extends RuntimeException {
    public NoFileProvidedException(String message) {
        super(message);
    }
}