package com.ondra.contenidos.exceptions;

/**
 * Excepción lanzada cuando se intenta finalizar compra con un carrito vacío.
 *
 * <p>Se utiliza en el proceso de checkout para validar que el carrito
 * contenga al menos un item antes de procesar el pago.</p>
 */
public class CarritoVacioException extends RuntimeException {

    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje mensaje descriptivo del error
     */
    public CarritoVacioException(String mensaje) {
        super(mensaje);
    }
}