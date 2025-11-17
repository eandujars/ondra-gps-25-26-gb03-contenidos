package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el formato del archivo de imagen no es válido.
 *
 * <p>Formatos válidos: JPG, PNG, WEBP</p>
 */
public class InvalidImageFormatException extends RuntimeException {
    public InvalidImageFormatException(String message) {
        super(message);
    }
}