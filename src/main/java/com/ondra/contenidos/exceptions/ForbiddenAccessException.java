package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso sin permisos.
 */
public class ForbiddenAccessException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error de acceso
     */
    public ForbiddenAccessException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param mensaje descripción del error de acceso
     * @param causa excepción original que causó el error
     */
    public ForbiddenAccessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}