package com.ondra.contenidos.models.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carrito_items",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"id_carrito", "id_cancion"}),
           @UniqueConstraint(columnNames = {"id_carrito", "id_album"})
       },
       indexes = {
           @Index(name = "idx_carrito_item_carrito", columnList = "id_carrito"),
           @Index(name = "idx_carrito_item_cancion", columnList = "id_cancion"),
           @Index(name = "idx_carrito_item_album", columnList = "id_album")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito_item")
    private Long idCarritoItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", nullable = false, length = 20)
    private TipoProducto tipoProducto;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "url_portada", length = 500)
    private String urlPortada;

    @Column(name = "nombre_artistico", length = 255)
    private String nombreArtistico;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    @PrePersist
    protected void onCreate() {
        fechaAgregado = LocalDateTime.now();
    }

    public enum TipoProducto {
        CANCION,
        ALBUM
    }

    /**
     * Valida que el item tenga exactamente un producto (canción o álbum)
     */
    public boolean esValido() {
        return (cancion != null && album == null && tipoProducto == TipoProducto.CANCION) ||
               (album != null && cancion == null && tipoProducto == TipoProducto.ALBUM);
    }
}