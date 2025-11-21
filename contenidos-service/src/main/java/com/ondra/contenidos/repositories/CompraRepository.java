package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Compra;
import com.ondra.contenidos.models.dao.Compra.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    /**
     * Buscar todas las compras de un usuario con paginación
     */
    Page<Compra> findByIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Buscar todas las compras de un usuario
     */
    List<Compra> findByIdUsuario(Long idUsuario);

    /**
     * Buscar compras de un usuario por tipo de contenido
     */
    Page<Compra> findByIdUsuarioAndTipoContenido(Long idUsuario, TipoContenido tipoContenido, Pageable pageable);

    /**
     * Buscar compras de canciones de un usuario
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario AND c.cancion IS NOT NULL")
    Page<Compra> findCancionesCompradasByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Buscar compras de álbumes de un usuario
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario AND c.album IS NOT NULL")
    Page<Compra> findAlbumesCompradosByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Verificar si un usuario ha comprado una canción
     */
    @Query("SELECT COUNT(c) > 0 FROM Compra c WHERE c.idUsuario = :idUsuario AND c.cancion.idCancion = :idCancion")
    boolean existsByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Verificar si un usuario ha comprado un álbum
     */
    @Query("SELECT COUNT(c) > 0 FROM Compra c WHERE c.idUsuario = :idUsuario AND c.album.idAlbum = :idAlbum")
    boolean existsByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Contar compras de un usuario
     */
    long countByIdUsuario(Long idUsuario);

    /**
     * Obtener total gastado por un usuario
     */
    @Query("SELECT COALESCE(SUM(c.precioPagado), 0) FROM Compra c WHERE c.idUsuario = :idUsuario")
    BigDecimal totalGastadoByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Obtener compras en un rango de fechas
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario AND c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    Page<Compra> findByUsuarioAndFechaBetween(
            @Param("idUsuario") Long idUsuario,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    /**
     * Contar compras de canciones de un usuario
     */
    @Query("SELECT COUNT(c) FROM Compra c WHERE c.idUsuario = :idUsuario AND c.cancion IS NOT NULL")
    long countCancionesCompradasByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Contar compras de álbumes de un usuario
     */
    @Query("SELECT COUNT(c) FROM Compra c WHERE c.idUsuario = :idUsuario AND c.album IS NOT NULL")
    long countAlbumesCompradosByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Obtener compras más recientes de un usuario
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario ORDER BY c.fechaCompra DESC")
    Page<Compra> findRecentesByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    void deleteByIdUsuario(Long idUsuario);
}