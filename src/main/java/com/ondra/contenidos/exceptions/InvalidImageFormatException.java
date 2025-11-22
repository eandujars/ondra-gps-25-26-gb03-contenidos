package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el formato del archivo de imagen no es válido.
 */
public class InvalidImageFormatException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message descripción del error
     */
    public InvalidImageFormatException(String message) {
        super(message);
    }
}