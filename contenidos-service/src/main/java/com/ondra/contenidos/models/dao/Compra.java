package com.ondra.contenidos.models.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    @Column(name = "precio_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPagado;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "id_transaccion", length = 255)
    private String idTransaccion;

    @PrePersist
    protected void onCreate() {
        fechaCompra = LocalDateTime.now();
    }

    public enum TipoContenido {
        CANCION,
        ALBUM
    }

    /**
     * Valida que la compra tenga exactamente un contenido (canción o álbum)
     */
    public boolean esValida() {
        return (cancion != null && album == null && tipoContenido == TipoContenido.CANCION) ||
               (album != null && cancion == null && tipoContenido == TipoContenido.ALBUM);
    }
}