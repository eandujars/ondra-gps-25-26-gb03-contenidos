package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando falla la subida de imagen a Cloudinary.
 */
public class ImageUploadFailedException extends RuntimeException {

    /**
     * Constructor con mensaje y causa del error.
     *
     * @param message descripción del error
     * @param cause excepción original que causó el fallo
     */
    public ImageUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}