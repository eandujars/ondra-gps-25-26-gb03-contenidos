package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un álbum por su identificador.
 *
 * <p>Se utiliza en operaciones de consulta, actualización y eliminación
 * de álbumes que no existen en la base de datos.</p>
 */
public class AlbumNotFoundException extends RuntimeException {

    /**
     * Constructor con identificador del álbum no encontrado.
     *
     * @param idAlbum identificador del álbum buscado
     */
    public AlbumNotFoundException(Long idAlbum) {
        super(String.format("No se encontró el álbum con ID: %d", idAlbum));
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje mensaje de error personalizado
     */
    public AlbumNotFoundException(String mensaje) {
        super(mensaje);
    }
}