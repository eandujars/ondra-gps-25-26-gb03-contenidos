package com.ondra.contenidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un género musical.
 *
 * <p>Contiene el identificador y nombre del género disponible en el sistema.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneroDTO {

    /**
     * Identificador único del género musical.
     */
    @JsonProperty("idGenero")
    private Long idGenero;

    /**
     * Nombre del género musical.
     */
    @JsonProperty("nombreGenero")
    private String nombreGenero;
}