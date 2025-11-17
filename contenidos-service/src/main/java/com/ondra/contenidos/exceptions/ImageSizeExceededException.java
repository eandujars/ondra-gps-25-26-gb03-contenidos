package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el tamaño del archivo de imagen excede el límite permitido.
 *
 * <p>Límite por defecto: 5MB</p>
 */
public class ImageSizeExceededException extends RuntimeException {
    public ImageSizeExceededException(String message) {
        super(message);
    }
}