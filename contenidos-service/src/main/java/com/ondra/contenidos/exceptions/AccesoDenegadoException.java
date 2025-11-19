package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta realizar una acción sobre
 * contenido que no le pertenece.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException() {
        super("No tienes permiso para realizar esta acción. " +
                "Solo el propietario del contenido puede modificarlo.");
    }

    public AccesoDenegadoException(String tipoContenido, Long idContenido) {
        super(String.format("No tienes permiso para modificar %s con ID %d. " +
                        "Solo el propietario puede realizar esta acción.",
                tipoContenido, idContenido));
    }

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}