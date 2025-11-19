package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta eliminar una canción que no está en el álbum.
 */
public class CancionNoEnAlbumException extends RuntimeException {

    public CancionNoEnAlbumException(Long idCancion, Long idAlbum) {
        super(String.format("La canción con ID %d no está en el álbum con ID %d",
                idCancion, idAlbum));
    }

    public CancionNoEnAlbumException(String mensaje) {
        super(mensaje);
    }
}