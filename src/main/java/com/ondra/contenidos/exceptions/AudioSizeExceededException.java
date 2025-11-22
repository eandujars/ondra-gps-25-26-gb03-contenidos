package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el tamaño del archivo de audio excede el límite permitido.
 *
 * <p>El límite máximo configurado es de 50MB para archivos de audio.</p>
 */
public class AudioSizeExceededException extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param message mensaje descriptivo del error
     */
    public AudioSizeExceededException(String message) {
        super(message);
    }
}