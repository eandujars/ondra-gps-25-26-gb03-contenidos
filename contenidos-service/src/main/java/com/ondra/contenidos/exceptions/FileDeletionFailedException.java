package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando falla la eliminación de un archivo de Cloudinary.
 */
public class FileDeletionFailedException extends RuntimeException {
    public FileDeletionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}