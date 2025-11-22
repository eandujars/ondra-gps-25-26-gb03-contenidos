package com.ondra.contenidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para editar un comentario existente.
 *
 * <p>Permite modificar únicamente el texto del comentario.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarComentarioDTO {

    /**
     * Nuevo texto del comentario.
     */
    @NotBlank(message = "El contenido del comentario es obligatorio")
    @Size(max = 1000, message = "El comentario no puede exceder los 1000 caracteres")
    private String contenido;
}