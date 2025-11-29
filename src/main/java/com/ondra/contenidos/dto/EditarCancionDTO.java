package com.ondra.contenidos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la edición de una canción existente.
 *
 * <p>Todos los campos son opcionales. Solo se actualizan
 * los campos que se proporcionen en la petición.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditarCancionDTO {

    /**
     * Título de la canción.
     */
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String tituloCancion;

    /**
     * Identificador del género musical.
     */
    private Long idGenero;

    /**
     * Precio de la canción en euros.
     */
    @Min(value = 0, message = "El precio no puede ser negativo")
    @DecimalMax(value = "999.99", message = "El precio no puede exceder 999.99€")
    private Double precioCancion;

    /**
     * URL de la portada de la canción en Cloudinary.
     */
    @Size(max = 500, message = "La URL de portada no puede exceder 500 caracteres")
    @Pattern(
            regexp = "^https://res\\.cloudinary\\.com/.*",
            message = "La URL debe ser de Cloudinary"
    )
    private String urlPortada;

    /**
     * Descripción de la canción.
     */
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;
}