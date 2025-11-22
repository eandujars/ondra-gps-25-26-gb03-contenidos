package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta crear una valoración duplicada.
 */
public class ValoracionYaExisteException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public ValoracionYaExisteException(String mensaje) {
        super(mensaje);
    }
}