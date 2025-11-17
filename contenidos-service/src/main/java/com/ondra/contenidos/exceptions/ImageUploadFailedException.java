package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando falla la subida de la imagen a Cloudinary.
 */
public class ImageUploadFailedException extends RuntimeException {
    public ImageUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}