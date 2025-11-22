package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Valoracion;
import com.ondra.contenidos.models.enums.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de valoraciones de contenidos musicales.
 *
 * <p>Proporciona operaciones de consulta, validación y eliminación de valoraciones
 * sobre canciones y álbumes, incluyendo cálculos de promedios y estadísticas.</p>
 */
@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    /**
     * Obtiene las valoraciones de un usuario con paginación.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de valoraciones del usuario
     */
    Page<Valoracion> findByIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Obtiene todas las valoraciones de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista completa de valoraciones
     */
    List<Valoracion> findByIdUsuario(Long idUsuario);

    /**
     * Obtiene las valoraciones de una canción ordenadas por fecha descendente.
     *
     * @param idCancion identificador de la canción
     * @param pageable configuración de paginación
     * @return página de valoraciones de la canción
     */
    @Query("SELECT v FROM Valoracion v WHERE v.cancion.idCancion = :idCancion ORDER BY v.fechaValoracion DESC")
    Page<Valoracion> findByCancion(@Param("idCancion") Long idCancion, Pageable pageable);

    /**
     * Obtiene las valoraciones de un álbum ordenadas por fecha descendente.
     *
     * @param idAlbum identificador del álbum
     * @param pageable configuración de paginación
     * @return página de valoraciones del álbum
     */
    @Query("SELECT v FROM Valoracion v WHERE v.album.idAlbum = :idAlbum ORDER BY v.fechaValoracion DESC")
    Page<Valoracion> findByAlbum(@Param("idAlbum") Long idAlbum, Pageable pageable);

    /**
     * Obtiene todas las valoraciones de una canción sin paginación.
     *
     * @param idCancion identificador de la canción
     * @return lista completa de valoraciones
     */
    @Query("SELECT v FROM Valoracion v WHERE v.cancion.idCancion = :idCancion")
    List<Valoracion> findByCancionList(@Param("idCancion") Long idCancion);

    /**
     * Obtiene todas las valoraciones de un álbum sin paginación.
     *
     * @param idAlbum identificador del álbum
     * @return lista completa de valoraciones
     */
    @Query("SELECT v FROM Valoracion v WHERE v.album.idAlbum = :idAlbum")
    List<Valoracion> findByAlbumList(@Param("idAlbum") Long idAlbum);

    /**
     * Obtiene valoraciones de un usuario filtradas por tipo de contenido.
     *
     * @param idUsuario identificador del usuario
     * @param tipoContenido tipo de contenido a filtrar
     * @param pageable configuración de paginación
     * @return página de valoraciones filtradas
     */
    Page<Valoracion> findByIdUsuarioAndTipoContenido(Long idUsuario, TipoContenido tipoContenido, Pageable pageable);

    /**
     * Verifica si un usuario ha valorado una canción.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return true si existe la valoración
     */
    @Query("SELECT COUNT(v) > 0 FROM Valoracion v WHERE v.idUsuario = :idUsuario AND v.cancion.idCancion = :idCancion")
    boolean existsByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Verifica si un usuario ha valorado un álbum.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return true si existe la valoración
     */
    @Query("SELECT COUNT(v) > 0 FROM Valoracion v WHERE v.idUsuario = :idUsuario AND v.album.idAlbum = :idAlbum")
    boolean existsByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Busca la valoración específica de un usuario para una canción.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return Optional con la valoración si existe
     */
    @Query("SELECT v FROM Valoracion v WHERE v.idUsuario = :idUsuario AND v.cancion.idCancion = :idCancion")
    Optional<Valoracion> findByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Busca la valoración específica de un usuario para un álbum.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return Optional con la valoración si existe
     */
    @Query("SELECT v FROM Valoracion v WHERE v.idUsuario = :idUsuario AND v.album.idAlbum = :idAlbum")
    Optional<Valoracion> findByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Calcula el promedio de valoraciones de una canción.
     *
     * @param idCancion identificador de la canción
     * @return promedio de valoraciones o null si no hay valoraciones
     */
    @Query("SELECT AVG(v.valor) FROM Valoracion v WHERE v.cancion.idCancion = :idCancion")
    Double calcularPromedioCancion(@Param("idCancion") Long idCancion);

    /**
     * Calcula el promedio de valoraciones de un álbum.
     *
     * @param idAlbum identificador del álbum
     * @return promedio de valoraciones o null si no hay valoraciones
     */
    @Query("SELECT AVG(v.valor) FROM Valoracion v WHERE v.album.idAlbum = :idAlbum")
    Double calcularPromedioAlbum(@Param("idAlbum") Long idAlbum);

    /**
     * Cuenta el total de valoraciones de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de valoraciones
     */
    long countByIdUsuario(Long idUsuario);

    /**
     * Cuenta el total de valoraciones de una canción.
     *
     * @param idCancion identificador de la canción
     * @return número de valoraciones
     */
    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.cancion.idCancion = :idCancion")
    long countByCancion(@Param("idCancion") Long idCancion);

    /**
     * Cuenta el total de valoraciones de un álbum.
     *
     * @param idAlbum identificador del álbum
     * @return número de valoraciones
     */
    @Query("SELECT COUNT(v) FROM Valoracion v WHERE v.album.idAlbum = :idAlbum")
    long countByAlbum(@Param("idAlbum") Long idAlbum);

    /**
     * Elimina todas las valoraciones de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    void deleteByIdUsuario(Long idUsuario);

    /**
     * Elimina todas las valoraciones de una canción.
     *
     * @param idCancion identificador de la canción
     */
    @Query("DELETE FROM Valoracion v WHERE v.cancion.idCancion = :idCancion")
    void deleteByCancion(@Param("idCancion") Long idCancion);

    /**
     * Elimina todas las valoraciones de un álbum.
     *
     * @param idAlbum identificador del álbum
     */
    @Query("DELETE FROM Valoracion v WHERE v.album.idAlbum = :idAlbum")
    void deleteByAlbum(@Param("idAlbum") Long idAlbum);

    /**
     * Verifica si existe una valoración por identificador y usuario.
     *
     * @param idValoracion identificador de la valoración
     * @param idUsuario identificador del usuario
     * @return true si el usuario es propietario de la valoración
     */
    boolean existsByIdValoracionAndIdUsuario(Long idValoracion, Long idUsuario);
}