package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO básico de método de cobro devuelto por el microservicio de Usuarios.
 *
 * <p>Contiene información mínima necesaria para procesar pagos a artistas.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetodoCobroBasicoDTO {

    /**
     * Identificador del método de cobro.
     */
    private Long idMetodoCobro;

    /**
     * Tipo de método de cobro.
     */
    private String tipo;
}