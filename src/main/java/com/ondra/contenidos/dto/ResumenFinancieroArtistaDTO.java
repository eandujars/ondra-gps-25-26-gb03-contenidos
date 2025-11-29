package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para resumen financiero consolidado de un artista.
 *
 * <p>Proporciona una visión completa de los ingresos del artista,
 * desglosados por estado de pago y tipo de cobro.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenFinancieroArtistaDTO {

    /**
     * Identificador del artista.
     */
    private Long idArtista;

    /**
     * Total de ingresos generados.
     */
    private BigDecimal totalIngresos;

    /**
     * Total de cobros pendientes de pago.
     */
    private BigDecimal totalPendiente;

    /**
     * Total de cobros ya pagados.
     */
    private BigDecimal totalPagado;

    /**
     * Cantidad total de cobros por compra.
     */
    private Integer totalCobrosCompra;

    /**
     * Cantidad total de cobros por reproducción.
     */
    private Integer totalCobrosReproduccion;

    /**
     * Fecha y hora de generación del resumen.
     */
    private LocalDateTime fechaConsulta;
}