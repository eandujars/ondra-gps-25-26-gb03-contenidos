package com.ondra.contenidos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la edición de un álbum existente.
 * Todos los campos son opcionales.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditarAlbumDTO {

    /**
     * Título del álbum. Máximo 200 caracteres.
     */
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String tituloAlbum;

    /**
     * Identificador del género musical del álbum.
     */
    private Long idGenero;

    /**
     * Precio del álbum en euros. Rango válido: 0.00 - 9999.99.
     */
    @Min(value = 0, message = "El precio no puede ser negativo")
    @DecimalMax(value = "9999.99", message = "El precio no puede exceder 9999.99€")
    private Double precioAlbum;

    /**
     * URL de la imagen de portada almacenada en Cloudinary. Máximo 500 caracteres.
     */
    @Size(max = 500, message = "La URL no puede exceder 500 caracteres")
    @Pattern(
            regexp = "^https://res\\.cloudinary\\.com/.*",
            message = "La URL debe ser de Cloudinary"
    )
    private String urlPortada;

    /**
     * Descripción o sinopsis del álbum. Máximo 2000 caracteres.
     */
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String descripcion;
}