package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el formato del archivo de audio no es válido.
 */
public class InvalidAudioFormatException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message descripción del error
     */
    public InvalidAudioFormatException(String message) {
        super(message);
    }
}