package com.ondra.contenidos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la edición de una canción existente.
 * Todos los campos son opcionales, solo se actualizan los proporcionados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarCancionDTO {

    /**
     * Nuevo título de la canción (opcional).
     */
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String tituloCancion;

    /**
     * Nuevo ID de género (opcional).
     */
    private Long idGenero;

    /**
     * Nuevo precio (opcional).
     */
    @Min(value = 0, message = "El precio no puede ser negativo")
    @DecimalMax(value = "999.99", message = "El precio no puede exceder 999.99€")
    private Double precioCancion;

    /**
     * Nueva URL de portada (opcional).
     */
    @Size(max = 500, message = "La URL de portada no puede exceder 500 caracteres")
    @Pattern(
            regexp = "^https://res\\.cloudinary\\.com/.*",
            message = "La URL debe ser de Cloudinary"
    )
    private String urlPortada;

    /**
     * Nueva descripción (opcional).
     */
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;
}