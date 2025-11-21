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

@Entity
@Table(name = "carritos",
       uniqueConstraints = @UniqueConstraint(columnNames = "id_usuario"),
       indexes = @Index(name = "idx_carrito_usuario", columnList = "id_usuario"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito")
    private Long idCarrito;

    @Column(name = "id_usuario", nullable = false, unique = true)
    private Long idUsuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CarritoItem> items = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    /**
     * Agregar un item al carrito
     */
    public void agregarItem(CarritoItem item) {
        items.add(item);
        item.setCarrito(this);
    }

    /**
     * Eliminar un item del carrito
     */
    public void eliminarItem(CarritoItem item) {
        items.remove(item);
        item.setCarrito(null);
    }

    /**
     * Obtener cantidad total de items
     */
    public int getCantidadItems() {
        return items.size();
    }

    /**
     * Calcular precio total del carrito
     */
    public BigDecimal getPrecioTotal() {
        return items.stream()
                .map(CarritoItem::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Vaciar el carrito
     */
    public void vaciar() {
        items.clear();
    }
}