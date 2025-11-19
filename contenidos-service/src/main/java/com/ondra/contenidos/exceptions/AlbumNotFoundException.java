package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un álbum por su ID.
 */
public class AlbumNotFoundException extends RuntimeException {

    public AlbumNotFoundException(Long idAlbum) {
        super(String.format("No se encontró el álbum con ID: %d", idAlbum));
    }

    public AlbumNotFoundException(String mensaje) {
        super(mensaje);
    }
}