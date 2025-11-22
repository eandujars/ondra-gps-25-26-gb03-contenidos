package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un género musical válido.
 */
public class GeneroNotFoundException extends RuntimeException {

    /**
     * Constructor con identificador del género.
     *
     * @param idGenero identificador del género no encontrado
     */
    public GeneroNotFoundException(Long idGenero) {
        super(String.format("No se encontró el género con ID: %d. " +
                "Los IDs válidos van del 1 al 30.", idGenero));
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public GeneroNotFoundException(String mensaje) {
        super(mensaje);
    }
}