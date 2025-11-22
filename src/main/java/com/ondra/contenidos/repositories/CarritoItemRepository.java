package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de items del carrito de compra.
 *
 * <p>Proporciona operaciones para añadir, consultar y eliminar contenidos
 * musicales dentro del carrito de un usuario.</p>
 */
@Repository
public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {

    /**
     * Obtiene todos los items de un carrito.
     *
     * @param idCarrito identificador del carrito
     * @return lista de items del carrito
     */
    List<CarritoItem> findByCarritoIdCarrito(Long idCarrito);

    /**
     * Busca un item de canción específico en el carrito.
     *
     * @param idCarrito identificador del carrito
     * @param idCancion identificador de la canción
     * @return Optional con el item si existe
     */
    @Query("SELECT ci FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.cancion.idCancion = :idCancion")
    Optional<CarritoItem> findByCarritoAndCancion(@Param("idCarrito") Long idCarrito, @Param("idCancion") Long idCancion);

    /**
     * Busca un item de álbum específico en el carrito.
     *
     * @param idCarrito identificador del carrito
     * @param idAlbum identificador del álbum
     * @return Optional con el item si existe
     */
    @Query("SELECT ci FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.album.idAlbum = :idAlbum")
    Optional<CarritoItem> findByCarritoAndAlbum(@Param("idCarrito") Long idCarrito, @Param("idAlbum") Long idAlbum);

    /**
     * Verifica si una canción está en el carrito.
     *
     * @param idCarrito identificador del carrito
     * @param idCancion identificador de la canción
     * @return true si la canción está en el carrito
     */
    @Query("SELECT COUNT(ci) > 0 FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.cancion.idCancion = :idCancion")
    boolean existsByCarritoAndCancion(@Param("idCarrito") Long idCarrito, @Param("idCancion") Long idCancion);

    /**
     * Verifica si un álbum está en el carrito.
     *
     * @param idCarrito identificador del carrito
     * @param idAlbum identificador del álbum
     * @return true si el álbum está en el carrito
     */
    @Query("SELECT COUNT(ci) > 0 FROM CarritoItem ci WHERE ci.carrito.idCarrito = :idCarrito AND ci.album.idAlbum = :idAlbum")
    boolean existsByCarritoAndAlbum(@Param("idCarrito") Long idCarrito, @Param("idAlbum") Long idAlbum);

    /**
     * Elimina todos los items de un carrito.
     *
     * @param idCarrito identificador del carrito
     */
    void deleteByCarritoIdCarrito(Long idCarrito);
}