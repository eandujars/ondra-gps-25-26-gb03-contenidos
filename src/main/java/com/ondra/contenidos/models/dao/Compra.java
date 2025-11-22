package com.ondra.contenidos.models.dao;

import com.ondra.contenidos.models.enums.TipoContenido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una compra realizada por un usuario.
 *
 * <p>Registra la adquisición de canciones o álbumes, incluyendo información
 * de pago y transacción. Cada compra está asociada a un único producto.</p>
 */
@Entity
@Table(name = "compras",
        indexes = {
                @Index(name = "idx_compra_usuario", columnList = "id_usuario"),
                @Index(name = "idx_compra_fecha", columnList = "fecha_compra"),
                @Index(name = "idx_compra_tipo", columnList = "tipo_contenido")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    /**
     * Identificador único de la compra.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    /**
     * Identificador del usuario que realizó la compra.
     */
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    /**
     * Canción comprada.
     * Null si la compra es de un álbum.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    /**
     * Álbum comprado.
     * Null si la compra es de una canción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    /**
     * Tipo de contenido comprado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    /**
     * Precio pagado por el producto al momento de la compra.
     */
    @Column(name = "precio_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPagado;

    /**
     * Fecha y hora de la compra.
     */
    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    /**
     * Método de pago utilizado.
     */
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    /**
     * Identificador de la transacción de pago.
     */
    @Column(name = "id_transaccion", length = 255)
    private String idTransaccion;

    /**
     * Establece la fecha de compra al crear el registro.
     */
    @PrePersist
    protected void onCreate() {
        fechaCompra = LocalDateTime.now();
    }

    /**
     * Valida que la compra tenga exactamente un contenido.
     *
     * @return true si la compra tiene solo canción o solo álbum
     */
    public boolean esValida() {
        return (cancion != null && album == null && tipoContenido == TipoContenido.CANCION) ||
                (album != null && cancion == null && tipoContenido == TipoContenido.ALBUM);
    }
}