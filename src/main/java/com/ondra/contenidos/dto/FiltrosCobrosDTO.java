package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para aplicar filtros avanzados en búsquedas de cobros.
 *
 * <p>Permite filtrar por artista, estado, tipo, fechas, montos
 * y aplicar ordenamiento con paginación.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiltrosCobrosDTO {

    /**
     * Identificador del artista.
     */
    private Long idArtista;

    /**
     * Estado del cobro (PENDIENTE, PAGADO, CANCELADO).
     */
    private String estado;

    /**
     * Tipo de cobro (COMPRA, REPRODUCCION).
     */
    private String tipoCobro;

    /**
     * Tipo de contenido (CANCION, ÁLBUM).
     */
    private String tipoContenido;

    /**
     * Fecha inicial del rango de búsqueda.
     */
    private LocalDateTime fechaDesde;

    /**
     * Fecha final del rango de búsqueda.
     */
    private LocalDateTime fechaHasta;

    /**
     * Mes específico para filtrar (1-12).
     */
    private Integer mes;

    /**
     * Año específico para filtrar.
     */
    private Integer anio;

    /**
     * Monto mínimo para filtrar.
     */
    private BigDecimal montoMinimo;

    /**
     * Monto máximo para filtrar.
     */
    private BigDecimal montoMaximo;

    /**
     * Campo por el cual ordenar (FECHA, MONTO).
     */
    private String ordenarPor;

    /**
     * Dirección del ordenamiento (ASC, DESC).
     */
    private String direccion;

    /**
     * Número de página solicitada.
     */
    private Integer pagina;

    /**
     * Límite de elementos por página.
     */
    private Integer limite;
}