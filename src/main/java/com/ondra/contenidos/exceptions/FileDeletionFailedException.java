package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando falla la eliminación de un archivo en Cloudinary.
 */
public class FileDeletionFailedException extends RuntimeException {

    /**
     * Constructor con mensaje y causa del error.
     *
     * @param message descripción del error
     * @param cause excepción original que causó el fallo
     */
    public FileDeletionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}