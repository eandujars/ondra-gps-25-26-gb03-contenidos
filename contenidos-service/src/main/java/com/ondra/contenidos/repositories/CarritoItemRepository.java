package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    /**
     * Buscar todos los items de un carrito
     */
    List<CarritoItem> findByCarritoIdCarrito(Long idCarrito);

    /**
     * Buscar item específico de canción en carrito
     */
    @Query("SELECT ci FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.cancion.idCancion = :idCancion")
    Optional<CarritoItem> findByCarritoAndCancion(@Param("idCarrito") Long idCarrito, @Param("idCancion") Long idCancion);

    /**
     * Buscar item específico de álbum en carrito
     */
    @Query("SELECT ci FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.album.idAlbum = :idAlbum")
    Optional<CarritoItem> findByCarritoAndAlbum(@Param("idCarrito") Long idCarrito, @Param("idAlbum") Long idAlbum);

    /**
     * Verificar si una canción ya está en el carrito
     */
    @Query("SELECT COUNT(ci) > 0 FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.cancion.idCancion = :idCancion")
    boolean existsByCarritoAndCancion(@Param("idCarrito") Long idCarrito, @Param("idCancion") Long idCancion);

    /**
     * Verificar si un álbum ya está en el carrito
     */
    @Query("SELECT COUNT(ci) > 0 FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.album.idAlbum = :idAlbum")
    boolean existsByCarritoAndAlbum(@Param("idCarrito") Long idCarrito, @Param("idAlbum") Long idAlbum);

    /**
     * Eliminar todos los items de un carrito
     */
    void deleteByCarritoIdCarrito(Long idCarrito);
}