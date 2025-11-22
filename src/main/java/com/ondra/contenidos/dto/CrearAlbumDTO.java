package com.ondra.contenidos.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creación de un nuevo álbum.
 *
 * <p>Contiene las validaciones necesarias para asegurar la integridad
 * de los datos del álbum antes de su almacenamiento.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearAlbumDTO {

    /**
     * Título del álbum.
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String tituloAlbum;

    /**
     * Identificador del género musical predominante.
     */
    @NotNull(message = "El género es obligatorio")
    private Long idGenero;

    /**
     * Precio del álbum completo en euros.
     * Valor 0.00 para álbumes gratuitos.
     */
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @DecimalMax(value = "9999.99", message = "El precio no puede exceder 9999.99€")
    private Double precioAlbum;

    /**
     * URL de la portada del álbum en Cloudinary.
     */
    @NotBlank(message = "La URL de portada es obligatoria")
    @Size(max = 500, message = "La URL no puede exceder 500 caracteres")
    @Pattern(
            regexp = "^https://res\\.cloudinary\\.com/.*",
            message = "La URL debe ser de Cloudinary"
    )
    private String urlPortada;

    /**
     * Descripción del álbum.
     */
    @Size(max = 2000, message = "La descripción no puede exceder 2000 caracteres")
    private String descripcion;
}