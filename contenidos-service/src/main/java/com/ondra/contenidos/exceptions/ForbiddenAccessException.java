package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso
 * para el cual no tiene permisos.
 *
 * Ejemplos:
 * - Usuario normal intentando acceder a endpoints de artista
 * - Artista intentando modificar contenido de otro artista
 * - Usuario sin idArtista en el token intentando crear/editar álbumes
 */
public class ForbiddenAccessException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje Descripción del error de acceso
     */
    public ForbiddenAccessException(String mensaje) {
        super(mensaje);
    }

    /**
     * Constructor con mensaje y causa.
     *
     * @param mensaje Descripción del error de acceso
     * @param causa Excepción original que causó el error
     */
    public ForbiddenAccessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}