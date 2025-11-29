package com.ondra.contenidos.models.dao;

import com.ondra.contenidos.models.enums.EstadoCobro;
import com.ondra.contenidos.models.enums.TipoCobro;
import com.ondra.contenidos.models.enums.TipoContenido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un cobro generado para un artista.
 *
 * <p>Los cobros se generan automáticamente por:</p>
 * <ul>
 *   <li>Compras de canciones o álbumes (80% del precio de venta)</li>
 *   <li>Reproducciones acumuladas cada 1000 plays (tarifa fija configurada)</li>
 * </ul>
 *
 * <p>El estado del cobro pasa de PENDIENTE a PAGADO cuando se procesa el pago mensual.</p>
 */
@Entity
@Table(name = "cobros",
        indexes = {
                @Index(name = "idx_cobro_artista", columnList = "id_artista"),
                @Index(name = "idx_cobro_estado", columnList = "estado"),
                @Index(name = "idx_cobro_fecha", columnList = "fecha_cobro")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cobro {

    /**
     * Identificador único del cobro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cobro")
    private Long idCobro;

    /**
     * Identificador del artista que recibe el cobro.
     */
    @Column(name = "id_artista", nullable = false)
    private Long idArtista;

    /**
     * Tipo de cobro (COMPRA, REPRODUCCION).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobro", nullable = false, length = 20)
    private TipoCobro tipoCobro;

    /**
     * Monto a cobrar al artista.
     */
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /**
     * Fecha y hora en que se generó el cobro.
     */
    @Column(name = "fecha_cobro", nullable = false)
    private LocalDateTime fechaCobro;

    /**
     * Compra asociada si el cobro se generó por una compra de contenido.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra")
    private Compra compra;

    /**
     * Tipo de contenido que generó el cobro (CANCION, ÁLBUM).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", length = 20)
    private TipoContenido tipoContenido;

    /**
     * Identificador de la canción si aplica.
     */
    @Column(name = "id_cancion")
    private Long idCancion;

    /**
     * Identificador del álbum si aplica.
     */
    @Column(name = "id_album")
    private Long idAlbum;

    /**
     * Número de reproducciones acumuladas que generaron el cobro.
     * Aplica solo para cobros por reproducción.
     */
    @Column(name = "reproducciones_acumuladas")
    private Long reproduccionesAcumuladas;

    /**
     * Estado actual del cobro (PENDIENTE, PAGADO, CANCELADO).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCobro estado = EstadoCobro.PENDIENTE;

    /**
     * Identificador del método de cobro usado para pagar al artista.
     * Null cuando el cobro está pendiente, se establece al marcar como pagado.
     */
    @Column(name = "id_metodo_cobro")
    private Long idMetodoCobro;

    /**
     * Fecha y hora en que se realizó el pago al artista.
     * Null cuando el cobro está pendiente.
     */
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    /**
     * Descripción adicional del cobro.
     */
    @Column(name = "descripcion")
    private String descripcion;

    /**
     * Inicializa valores por defecto antes de persistir la entidad.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaCobro == null) {
            fechaCobro = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoCobro.PENDIENTE;
        }
    }
}