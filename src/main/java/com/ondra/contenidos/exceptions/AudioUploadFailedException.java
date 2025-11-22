package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando falla la subida del archivo de audio a Cloudinary.
 *
 * <p>Encapsula errores de red, validación o procesamiento durante
 * la operación de subida a Cloudinary.</p>
 */
public class AudioUploadFailedException extends RuntimeException {

    /**
     * Constructor con mensaje y causa del error.
     *
     * @param message mensaje descriptivo del error
     * @param cause excepción que causó el fallo
     */
    public AudioUploadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}