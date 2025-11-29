package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para resumen agregado de cobros por período mensual.
 *
 * <p>Proporciona estadísticas consolidadas de cobros agrupados por mes,
 * incluyendo totales y desgloses por estado de pago.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenCobrosDTO {

    /**
     * Mes del resumen (1-12).
     */
    private Integer mes;

    /**
     * Año del resumen.
     */
    private Integer anio;

    /**
     * Monto total de cobros en el período.
     */
    private BigDecimal totalCobros;

    /**
     * Cantidad total de cobros en el período.
     */
    private Long cantidadCobros;

    /**
     * Monto total pendiente de pago.
     */
    private BigDecimal montoPendiente;

    /**
     * Monto total ya pagado.
     */
    private BigDecimal montoPagado;
}