package com.ondra.contenidos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para editar una valoración existente.
 *
 * <p>Permite modificar únicamente el valor de la valoración en estrellas.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarValoracionDTO {

    /**
     * Nuevo valor de la valoración en escala de 1 a 5 estrellas.
     */
    @NotNull(message = "El valor de la valoración es obligatorio")
    @Min(value = 1, message = "La valoración mínima es 1")
    @Max(value = 5, message = "La valoración máxima es 5")
    private Integer valor;
}