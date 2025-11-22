package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta añadir una canción que ya pertenece al álbum.
 *
 * <p>Se utiliza en operaciones de gestión de tracklist para prevenir
 * duplicados de canciones dentro de un mismo álbum.</p>
 */
public class CancionYaEnAlbumException extends RuntimeException {

    /**
     * Constructor con identificadores de canción y álbum.
     *
     * @param idCancion identificador de la canción
     * @param idAlbum identificador del álbum
     */
    public CancionYaEnAlbumException(Long idCancion, Long idAlbum) {
        super(String.format("La canción con ID %d ya está en el álbum con ID %d",
                idCancion, idAlbum));
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje mensaje de error personalizado
     */
    public CancionYaEnAlbumException(String mensaje) {
        super(mensaje);
    }
}