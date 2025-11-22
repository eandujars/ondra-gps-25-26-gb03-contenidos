package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta realizar una acción sobre
 * contenido que no le pertenece.
 *
 * <p>Se utiliza para proteger operaciones de modificación y eliminación
 * de canciones, álbumes y otros recursos del sistema.</p>
 */
public class AccesoDenegadoException extends RuntimeException {

    /**
     * Constructor con mensaje predeterminado de acceso denegado.
     */
    public AccesoDenegadoException() {
        super("No tienes permiso para realizar esta acción. " +
                "Solo el propietario del contenido puede modificarlo.");
    }

    /**
     * Constructor con tipo de contenido e identificador.
     *
     * @param tipoContenido tipo de recurso (canción, álbum, etc.)
     * @param idContenido identificador del contenido
     */
    public AccesoDenegadoException(String tipoContenido, Long idContenido) {
        super(String.format("No tienes permiso para modificar %s con ID %d. " +
                        "Solo el propietario puede realizar esta acción.",
                tipoContenido, idContenido));
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje mensaje de error personalizado
     */
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}