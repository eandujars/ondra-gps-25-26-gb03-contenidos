package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el tamaño de la imagen excede el límite permitido.
 */
public class ImageSizeExceededException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param message descripción del error
     */
    public ImageSizeExceededException(String message) {
        super(message);
    }
}