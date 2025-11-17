package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta genérico para operaciones exitosas.
 *
 * <p>Se utiliza para endpoints de eliminación y otras operaciones
 * que no devuelven datos específicos.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessfulResponseDTO {

    /**
     * Título o resumen de la operación exitosa.
     */
    private String successful;

    /**
     * Mensaje descriptivo detallado de la operación.
     */
    private String message;

    /**
     * Código de estado HTTP de la respuesta.
     */
    private int statusCode;

    /**
     * Timestamp de cuando se generó la respuesta (formato ISO).
     */
    private String timestamp;
}