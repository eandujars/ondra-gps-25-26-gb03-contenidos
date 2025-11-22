package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando el número de pista ya existe en el álbum.
 */
public class NumeroPistaYaExisteException extends RuntimeException {

    /**
     * Constructor con número de pista e identificador del álbum.
     *
     * @param numeroPista número de pista duplicado
     * @param idAlbum identificador del álbum
     */
    public NumeroPistaYaExisteException(Integer numeroPista, Long idAlbum) {
        super(String.format("El número de pista %d ya existe en el álbum con ID %d. " +
                        "Cada número de pista debe ser único dentro del álbum.",
                numeroPista, idAlbum));
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * @param mensaje descripción del error
     */
    public NumeroPistaYaExisteException(String mensaje) {
        super(mensaje);
    }
}