package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra una valoración en el sistema.
 */
public class ValoracionNotFoundException extends RuntimeException {

    /**
     * Constructor con identificador de la valoración.
     *
     * @param idValoracion identificador de la valoración no encontrada
     */
    public ValoracionNotFoundException(Long idValoracion) {
        super("Valoración no encontrada con ID: " + idValoracion);
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public ValoracionNotFoundException(String mensaje) {
        super(mensaje);
    }
}