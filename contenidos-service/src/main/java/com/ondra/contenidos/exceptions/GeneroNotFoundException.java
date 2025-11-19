package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se proporciona un ID de género inválido.
 */
public class GeneroNotFoundException extends RuntimeException {

    public GeneroNotFoundException(Long idGenero) {
        super(String.format("No se encontró el género con ID: %d. " +
                "Los IDs válidos van del 1 al 30.", idGenero));
    }

    public GeneroNotFoundException(String mensaje) {
        super(mensaje);
    }
}