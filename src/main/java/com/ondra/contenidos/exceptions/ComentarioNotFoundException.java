package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un comentario en el sistema.
 */
public class ComentarioNotFoundException extends RuntimeException {

    /**
     * Constructor con identificador del comentario.
     *
     * @param idComentario identificador del comentario no encontrado
     */
    public ComentarioNotFoundException(Long idComentario) {
        super("Comentario no encontrado con ID: " + idComentario);
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public ComentarioNotFoundException(String mensaje) {
        super(mensaje);
    }
}