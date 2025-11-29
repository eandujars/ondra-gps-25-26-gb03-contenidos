package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar un cobro individual de un artista.
 *
 * <p>Contiene información detallada sobre cobros generados por compras
 * o reproducciones de contenido, incluyendo estado de pago y referencias.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CobroDTO {

    /**
     * Identificador único del cobro.
     */
    private Long idCobro;

    /**
     * Identificador del artista que recibe el cobro.
     */
    private Long idArtista;

    /**
     * Tipo de cobro (COMPRA, REPRODUCCION).
     */
    private String tipoCobro;

    /**
     * Monto a cobrar.
     */
    private BigDecimal monto;

    /**
     * Fecha en que se generó el cobro.
     */
    private LocalDateTime fechaCobro;

    /**
     * Tipo de contenido (CANCION, ÁLBUM).
     */
    private String tipoContenido;

    /**
     * Identificador de la canción si aplica.
     */
    private Long idCancion;

    /**
     * Identificador del álbum si aplica.
     */
    private Long idAlbum;

    /**
     * Título de la canción o álbum.
     */
    private String tituloContenido;

    /**
     * Número de reproducciones acumuladas si aplica.
     */
    private Long reproduccionesAcumuladas;

    /**
     * Estado del cobro (PENDIENTE, PAGADO, CANCELADO).
     */
    private String estado;

    /**
     * Identificador del método de cobro utilizado.
     */
    private Long idMetodoCobro;

    /**
     * Fecha en que se realizó el pago.
     */
    private LocalDateTime fechaPago;

    /**
     * Referencia del pago procesado.
     */
    private String referenciaPago;

    /**
     * Descripción adicional del cobro.
     */
    private String descripcion;

    /**
     * Identificador de la compra relacionada si aplica.
     */
    private Long idCompra;

    /**
     * Nombre descriptivo del método de cobro.
     */
    private String nombreMetodoCobro;
}