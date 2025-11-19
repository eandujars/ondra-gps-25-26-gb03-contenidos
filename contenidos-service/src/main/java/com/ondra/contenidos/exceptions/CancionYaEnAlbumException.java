package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta añadir una canción que ya está en el álbum.
 */
public class CancionYaEnAlbumException extends RuntimeException {

    public CancionYaEnAlbumException(Long idCancion, Long idAlbum) {
        super(String.format("La canción con ID %d ya está en el álbum con ID %d",
                idCancion, idAlbum));
    }

    public CancionYaEnAlbumException(String mensaje) {
        super(mensaje);
    }
}