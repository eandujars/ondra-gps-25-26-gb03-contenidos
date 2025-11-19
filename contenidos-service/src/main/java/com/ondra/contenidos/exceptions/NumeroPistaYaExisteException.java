package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta usar un número de pista que ya existe en el álbum.
 */
public class NumeroPistaYaExisteException extends RuntimeException {

    public NumeroPistaYaExisteException(Integer numeroPista, Long idAlbum) {
        super(String.format("El número de pista %d ya existe en el álbum con ID %d. " +
                        "Cada número de pista debe ser único dentro del álbum.",
                numeroPista, idAlbum));
    }

    public NumeroPistaYaExisteException(String mensaje) {
        super(mensaje);
    }
}