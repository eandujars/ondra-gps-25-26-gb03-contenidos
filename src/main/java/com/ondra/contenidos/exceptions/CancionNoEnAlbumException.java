package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta eliminar una canción que no pertenece al álbum.
 *
 * <p>Se utiliza en operaciones de gestión de tracklist cuando se intenta
 * remover una canción que no está asociada al álbum especificado.</p>
 */
public class CancionNoEnAlbumException extends RuntimeException {

    /**
     * Constructor con identificadores de canción y álbum.
     *
     * @param idCancion identificador de la canción
     * @param idAlbum identificador del álbum
     */
    public CancionNoEnAlbumException(Long idCancion, Long idAlbum) {
        super(String.format("La canción con ID %d no está en el álbum con ID %d",
                idCancion, idAlbum));
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje mensaje de error personalizado
     */
    public CancionNoEnAlbumException(String mensaje) {
        super(mensaje);
    }
}