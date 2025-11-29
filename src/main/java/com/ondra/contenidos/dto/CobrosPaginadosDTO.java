package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para respuesta paginada de cobros con estadísticas agregadas.
 *
 * <p>Incluye la lista de cobros junto con metadatos de paginación
 * y totales calculados por estado de pago.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CobrosPaginadosDTO {

    /**
     * Lista de cobros de la página actual.
     */
    private List<CobroDTO> cobros;

    /**
     * Número de la página actual.
     */
    private Integer paginaActual;

    /**
     * Total de páginas disponibles.
     */
    private Integer totalPaginas;

    /**
     * Total de elementos en todas las páginas.
     */
    private Long totalElementos;

    /**
     * Número de elementos por página.
     */
    private Integer elementosPorPagina;

    /**
     * Monto total de todos los cobros.
     */
    private BigDecimal totalMonto;

    /**
     * Monto total de cobros pendientes.
     */
    private BigDecimal montoPendiente;

    /**
     * Monto total de cobros pagados.
     */
    private BigDecimal montoPagado;
}