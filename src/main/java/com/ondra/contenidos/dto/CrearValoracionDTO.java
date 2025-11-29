package com.ondra.contenidos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear una nueva valoración de una canción o álbum.
 *
 * <p>Requiere especificar el tipo de contenido, su identificador
 * y un valor de 1 a 5 estrellas.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearValoracionDTO {

    /**
     * Tipo de contenido a valorar.
     * Valores válidos: CANCION, ÁLBUM
     */
    @NotNull(message = "El tipo de contenido es obligatorio")
    private String tipoContenido;

    /**
     * Identificador de la canción.
     * Requerido cuando tipoContenido es CANCION.
     */
    private Long idCancion;

    /**
     * Identificador del álbum.
     * Requerido cuando tipoContenido es ÁLBUM.
     */
    private Long idAlbum;

    /**
     * Valor de la valoración en escala de 1 a 5 estrellas.
     */
    @NotNull(message = "El valor de la valoración es obligatorio")
    @Min(value = 1, message = "La valoración mínima es 1")
    @Max(value = 5, message = "La valoración máxima es 5")
    private Integer valor;
}