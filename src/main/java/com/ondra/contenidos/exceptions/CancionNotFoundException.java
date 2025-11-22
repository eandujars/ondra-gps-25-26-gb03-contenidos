package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra una canción por su identificador.
 *
 * <p>Se utiliza en operaciones de consulta, actualización y eliminación
 * de canciones que no existen en la base de datos.</p>
 */
public class CancionNotFoundException extends RuntimeException {

    /**
     * Constructor con identificador de la canción no encontrada.
     *
     * @param idCancion identificador de la canción buscada
     */
    public CancionNotFoundException(Long idCancion) {
        super(String.format("No se encontró la canción con ID: %d", idCancion));
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje mensaje de error personalizado
     */
    public CancionNotFoundException(String mensaje) {
        super(mensaje);
    }
}