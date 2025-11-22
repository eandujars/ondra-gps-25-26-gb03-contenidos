package com.ondra.contenidos.models.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa el carrito de compras de un usuario.
 *
 * <p>Cada usuario tiene un único carrito que contiene los items
 * (canciones o álbumes) que desea adquirir.</p>
 */
@Entity
@Table(name = "carritos",
        uniqueConstraints = @UniqueConstraint(columnNames = "id_usuario"),
        indexes = @Index(name = "idx_carrito_usuario", columnList = "id_usuario"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {

    /**
     * Identificador único del carrito.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Long idCarrito;

    /**
     * Identificador del usuario propietario del carrito.
     */
    @Column(name = "id_usuario", nullable = false, unique = true)
    private Long idUsuario;

    /**
     * Lista de items contenidos en el carrito.
     */
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CarritoItem> items = new ArrayList<>();

    /**
     * Fecha de creación del carrito.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización del carrito.
     */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Establece la fecha de creación y actualización al crear el carrito.
     */
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de actualización al modificar el carrito.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Agrega un item al carrito estableciendo la relación bidireccional.
     *
     * @param item item a agregar
     */
    public void agregarItem(CarritoItem item) {
        items.add(item);
        item.setCarrito(this);
    }

    /**
     * Elimina un item del carrito rompiendo la relación bidireccional.
     *
     * @param item item a eliminar
     */
    public void eliminarItem(CarritoItem item) {
        items.remove(item);
        item.setCarrito(null);
    }

    /**
     * Obtiene la cantidad total de items en el carrito.
     *
     * @return número de items
     */
    public int getCantidadItems() {
        return items.size();
    }

    /**
     * Calcula el precio total sumando los precios de todos los items.
     *
     * @return precio total del carrito
     */
    public BigDecimal getPrecioTotal() {
        return items.stream()
                .map(CarritoItem::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Elimina todos los items del carrito.
     */
    public void vaciar() {
        items.clear();
    }
}