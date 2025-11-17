package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el formato del archivo de audio no es válido.
 *
 * <p>Formatos válidos: MP3, WAV, FLAC, M4A, OGG</p>
 */
public class InvalidAudioFormatException extends RuntimeException {
    public InvalidAudioFormatException(String message) {
        super(message);
    }
}