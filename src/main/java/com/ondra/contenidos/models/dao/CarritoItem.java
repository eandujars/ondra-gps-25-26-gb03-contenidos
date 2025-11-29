package com.ondra.contenidos.models.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un item individual dentro del carrito de compras.
 *
 * <p>Cada item puede ser una canción o un álbum, pero nunca ambos simultáneamente.
 * Se almacena información desnormalizada del producto para optimizar consultas.</p>
 */
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

    /**
     * Identificador único del item del carrito.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito_item")
    private Long idCarritoItem;

    /**
     * Carrito al que pertenece este item.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito", nullable = false)
    private Carrito carrito;

    /**
     * Canción asociada al item.
     * Null si el item es un álbum.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    /**
     * Álbum asociado al item.
     * Null si el item es una canción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    /**
     * Tipo de producto del item.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", nullable = false, length = 20)
    private TipoProducto tipoProducto;

    /**
     * Precio del producto al momento de agregarlo al carrito.
     */
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * URL de la portada del producto.
     */
    @Column(name = "url_portada", length = 500)
    private String urlPortada;

    /**
     * Nombre artístico del propietario del producto.
     */
    @Column(name = "nombre_artistico", length = 255)
    private String nombreArtistico;

    /**
     * Título del producto.
     */
    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    /**
     * Fecha en que se agregó el item al carrito.
     */
    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    /**
     * Slug del artista.
     */
    @Column(name = "slug_artista")
    private String slugArtista;

    /**
     * Establece la fecha de agregación al crear el item.
     */
    @PrePersist
    protected void onCreate() {
        fechaAgregado = LocalDateTime.now();
    }

    /**
     * Tipos de producto que pueden agregarse al carrito.
     */
    public enum TipoProducto {
        CANCIÓN,
        ÁLBUM
    }

    /**
     * Valida que el item tenga exactamente un producto.
     *
     * @return true si el item tiene solo canción o solo álbum
     */
    public boolean esValido() {
        return (cancion != null && album == null && tipoProducto == TipoProducto.CANCIÓN) ||
                (album != null && cancion == null && tipoProducto == TipoProducto.ÁLBUM);
    }
}