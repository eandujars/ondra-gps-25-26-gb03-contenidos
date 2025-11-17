package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el tamaño del archivo de audio excede el límite permitido.
 *
 * <p>Límite por defecto: 50MB</p>
 */
public class AudioSizeExceededException extends RuntimeException {
    public AudioSizeExceededException(String message) {
        super(message);
    }
}