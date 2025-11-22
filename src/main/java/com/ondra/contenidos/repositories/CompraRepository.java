package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Compra;
import com.ondra.contenidos.models.enums.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestión de compras de contenidos musicales.
 *
 * <p>Proporciona operaciones para consultar historial de compras, verificar
 * propiedad de contenidos y obtener estadísticas de gasto por usuario.</p>
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    /**
     * Obtiene las compras de un usuario con paginación.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de compras del usuario
     */
    Page<Compra> findByIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Obtiene todas las compras de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista completa de compras
     */
    List<Compra> findByIdUsuario(Long idUsuario);

    /**
     * Obtiene compras de un usuario filtradas por tipo de contenido.
     *
     * @param idUsuario identificador del usuario
     * @param tipoContenido tipo de contenido a filtrar
     * @param pageable configuración de paginación
     * @return página de compras filtradas
     */
    Page<Compra> findByIdUsuarioAndTipoContenido(Long idUsuario, TipoContenido tipoContenido, Pageable pageable);

    /**
     * Obtiene las canciones compradas por un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de compras de canciones
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario AND c.cancion IS NOT NULL")
    Page<Compra> findCancionesCompradasByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Obtiene los álbumes comprados por un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de compras de álbumes
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario AND c.album IS NOT NULL")
    Page<Compra> findAlbumesCompradosByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Verifica si un usuario ha comprado una canción.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return true si el usuario ha comprado la canción
     */
    @Query("SELECT COUNT(c) > 0 FROM Compra c WHERE c.idUsuario = :idUsuario AND c.cancion.idCancion = :idCancion")
    boolean existsByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Verifica si un usuario ha comprado un álbum.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return true si el usuario ha comprado el álbum
     */
    @Query("SELECT COUNT(c) > 0 FROM Compra c WHERE c.idUsuario = :idUsuario AND c.album.idAlbum = :idAlbum")
    boolean existsByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Cuenta el total de compras de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de compras
     */
    long countByIdUsuario(Long idUsuario);

    /**
     * Calcula el total gastado por un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return suma total de compras o 0 si no hay compras
     */
    @Query("SELECT COALESCE(SUM(c.precioPagado), 0) FROM Compra c WHERE c.idUsuario = :idUsuario")
    BigDecimal totalGastadoByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Obtiene compras de un usuario en un rango de fechas.
     *
     * @param idUsuario identificador del usuario
     * @param fechaInicio fecha de inicio del rango
     * @param fechaFin fecha de fin del rango
     * @param pageable configuración de paginación
     * @return página de compras en el rango especificado
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario AND c.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    Page<Compra> findByUsuarioAndFechaBetween(
            @Param("idUsuario") Long idUsuario,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    /**
     * Cuenta las canciones compradas por un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de canciones compradas
     */
    @Query("SELECT COUNT(c) FROM Compra c WHERE c.idUsuario = :idUsuario AND c.cancion IS NOT NULL")
    long countCancionesCompradasByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Cuenta los álbumes comprados por un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de álbumes comprados
     */
    @Query("SELECT COUNT(c) FROM Compra c WHERE c.idUsuario = :idUsuario AND c.album IS NOT NULL")
    long countAlbumesCompradosByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Obtiene las compras más recientes de un usuario ordenadas por fecha.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de compras ordenadas por fecha descendente
     */
    @Query("SELECT c FROM Compra c WHERE c.idUsuario = :idUsuario ORDER BY c.fechaCompra DESC")
    Page<Compra> findRecentesByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Elimina todas las compras de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    void deleteByIdUsuario(Long idUsuario);
}