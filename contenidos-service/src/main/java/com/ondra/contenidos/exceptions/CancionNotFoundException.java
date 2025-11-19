package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra una canción por su ID.
 */
public class CancionNotFoundException extends RuntimeException {

    public CancionNotFoundException(Long idCancion) {
        super(String.format("No se encontró la canción con ID: %d", idCancion));
    }

    public CancionNotFoundException(String mensaje) {
        super(mensaje);
    }
}