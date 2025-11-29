package com.ondra.contenidos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de una nueva canción.
 *
 * <p>Contiene las validaciones necesarias para asegurar la integridad
 * de los datos de la canción antes de su almacenamiento.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearCancionDTO {

    /**
     * Título de la canción.
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String tituloCancion;

    /**
     * Identificador del género musical.
     */
    @NotNull(message = "El género es obligatorio")
    private Long idGenero;

    /**
     * Precio de la canción en euros.
     * Valor 0.00 para canciones gratuitas.
     */
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @DecimalMax(value = "999.99", message = "El precio no puede exceder 999.99€")
    private Double precioCancion;

    /**
     * Duración de la canción en segundos.
     */
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 segundo")
    @Max(value = 7200, message = "La duración no puede exceder 2 horas (7200 segundos)")
    private Integer duracionSegundos;

    /**
     * URL de la portada de la canción en Cloudinary.
     */
    @NotBlank(message = "La URL de portada es obligatoria")
    @Size(max = 500, message = "La URL de portada no puede exceder 500 caracteres")
    @Pattern(
            regexp = "^https://res\\.cloudinary\\.com/.*",
            message = "La URL debe ser de Cloudinary"
    )
    private String urlPortada;

    /**
     * URL del archivo de audio en Cloudinary.
     */
    @NotBlank(message = "La URL de audio es obligatoria")
    @Size(max = 500, message = "La URL de audio no puede exceder 500 caracteres")
    @Pattern(
            regexp = "^https://res\\.cloudinary\\.com/.*",
            message = "La URL debe ser de Cloudinary"
    )
    private String urlAudio;

    /**
     * Descripción de la canción.
     */
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;
}