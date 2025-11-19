package com.ondra.contenidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para añadir una canción a un álbum.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarCancionAlbumDTO {

    /**
     * ID de la canción a añadir al álbum.
     */
    @NotNull(message = "El ID de la canción es obligatorio")
    private Long idCancion;

    /**
     * Número de pista en el álbum (1-indexed).
     * Debe ser único dentro del álbum.
     */
    @NotNull(message = "El número de pista es obligatorio")
    @Min(value = 1, message = "El número de pista debe ser al menos 1")
    private Integer numeroPista;
}