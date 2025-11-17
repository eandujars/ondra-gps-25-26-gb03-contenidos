package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando falla la subida del archivo de audio a Cloudinary.
 */
public class AudioUploadFailedException extends RuntimeException {
    public AudioUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}