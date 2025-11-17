package com.ondra.contenidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un género musical
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneroDTO {

    @JsonProperty("idGenero")
    private Long idGenero;

    @JsonProperty("nombreGenero")
    private String nombreGenero;
}